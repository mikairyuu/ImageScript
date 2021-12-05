import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO


object Utils {
    fun toSepia(image: BufferedImage, sepiaIntensity: Int = 20): BufferedImage {
        val width = image.width
        val height = image.height
        val sepiaDepth = 20
        val imagePixels = image.getRGB(0, 0, width, height, null, 0, width)
        for (i in imagePixels.indices) {
            val color = imagePixels[i]
            var r = color shr 16 and 0xff
            var g = color shr 8 and 0xff
            var b = color and 0xff
            val gry = (r + g + b) / 3
            b = gry
            g = b
            r = g
            r = r + sepiaDepth * 2
            g = g + sepiaDepth
            if (r > 255) {
                r = 255
            }
            if (g > 255) {
                g = 255
            }
            if (b > 255) {
                b = 255
            }

            // Darken blue color to increase sepia effect
            b -= sepiaIntensity

            // normalize if out of bounds
            if (b < 0) {
                b = 0
            }
            if (b > 255) {
                b = 255
            }
            imagePixels[i] = (color and -0x1000000) + (r shl 16) + (g shl 8) + b
        }
        val res = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        res.setRGB(0, 0, width, height, imagePixels, 0, width)
        return res
    }

    fun invertImage(inputFile: BufferedImage): BufferedImage {
        for (x in 0 until inputFile.width) {
            for (y in 0 until inputFile.height) {
                val rgba = inputFile.getRGB(x, y)
                var col = Color(rgba, true)
                col = Color(
                    255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue()
                )
                inputFile.setRGB(x, y, col.getRGB())
            }
        }
        return inputFile
    }

    fun newBrightness(source: BufferedImage, brightnessPercentage: Float): BufferedImage {

        val bi = BufferedImage(
            source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB
        )

        val pixel = intArrayOf(0, 0, 0, 0)
        val hsbvals = floatArrayOf(0f, 0f, 0f)

        bi.graphics.drawImage(source, 0, 0, null)
        for (i in 0 until bi.height) {
            for (j in 0 until bi.width) {

                bi.raster.getPixel(j, i, pixel)

                Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], hsbvals)

                var newBrightness = hsbvals[2] * brightnessPercentage
                if (newBrightness > 1f) {
                    newBrightness = 1f
                }

                // create a new color with the new brightness
                val c = Color(Color.HSBtoRGB(hsbvals[0], hsbvals[1], newBrightness))

                bi.raster.setPixel(j, i, intArrayOf(c.red, c.green, c.blue, pixel[3]))
            }
        }

        return bi
    }
}


fun toComposeImage(m: Mat, converter: OpenCVFrameConverter.ToOrgOpenCvCoreMat): ImageBitmap {
    return Java2DFrameUtils.toBufferedImage(converter.convert(m)).toComposeImageBitmap()
}

fun convertToOrgOpenCvCoreMat(img: ImageBitmap): Mat {
    val awt = BufferedImage(img.width, img.height, BufferedImage.TYPE_3BYTE_BGR)
    awt.graphics.drawImage(img.toAwtImage(), 0, 0, null)
    val res = Mat(img.height, img.width, CvType.CV_8UC3)
    res.put(0, 0, (awt.raster.dataBuffer as DataBufferByte).data)
    return res
}

// NTid:XxY(ct|ct|;oc)
// ct (content)       -> 絵b64 or str
// oc (out.connector) -> id,ind
fun serialize(nodeContainer: SnapshotStateList<NodeObject>): String {
    val sb = StringBuilder()
    val encoder = Base64.getEncoder()
    for (node in nodeContainer) {
        sb.append("${node.nodeType.id}:${node.Xpos}x${node.Ypos}")
        sb.append('(')
        for (content in node.content) {
            if (content is ImageBitmap) {
                val byteArray = ByteArrayOutputStream()
                ImageIO.write(content.toAwtImage(), "png", byteArray)
                sb.append("絵${encoder.encodeToString(byteArray.toByteArray())}")
            } else {
                sb.append(content.toString())
            }
            sb.append('|')
        }
        sb.append(';')
        if (node.outputConnector.nodeConnection != null) {
            val inputIndex =
                node.outputConnector.nodeConnection!!.endNodeObject!!.inputConnectors.indexOfFirst { nodeConnector -> nodeConnector.nodeConnection == node.outputConnector.nodeConnection }
            sb.append("${nodeContainer.indexOf(node.outputConnector.nodeConnection!!.endNodeObject)},${inputIndex}")
        }
        sb.append(')')
    }
    //println(sb.toString())
    return sb.toString()
}

fun decode(data: String, callback: () -> Unit): SnapshotStateList<NodeObject> {
    val nodeContainer = SnapshotStateList<NodeObject>()
    val nodeList = data.split(')')
    val decoder = Base64.getDecoder()
    val nodeOutputs = mutableListOf<String>()
    for (nodeString in nodeList) {
        if (nodeString.isEmpty()) continue
        val nodeSplit = nodeString.split('(')
        val header = nodeSplit[0]
        nodeOutputs.add(nodeSplit[1].substringAfter(';'))
        val pos = header.substringAfter(':').split('x')
        val node =
            NodeObject(NodeTypeStore.getNodeType(header.substringBefore(':').toInt()), pos[0].toInt(), pos[1].toInt())
        nodeContainer.add(node)
        val contents = nodeSplit[1].substringBefore(';').split('|').toMutableList()
        contents.removeLast()
        for (i in contents.indices) {
            if (contents[i].isNotEmpty()) {
                if (contents[i][0] == '絵') {
                    node.content[i] = ImageIO.read(
                        ByteArrayInputStream(decoder.decode(contents[i].substring(1, contents[i].length - 1)))
                    ).toComposeImageBitmap()
                    continue
                }
            }
            when (node.content[i]) {
                is Int -> {
                    node.content[i] = contents[i].toInt()
                }
                is Float -> {
                    node.content[i] = contents[i].toFloat()
                }
                is ImageBitmap? -> {
                    node.content[i] = null as ImageBitmap?
                }
                else -> {
                    node.content[i] = contents[i] // String
                }
            }
        }
    }

    for (i in nodeOutputs.indices) {
        if (nodeOutputs[i].isEmpty()) continue
        val content = nodeOutputs[i].split(',')
        val targetNode = nodeContainer[content[0].toInt()]
        val nodeConnection = NodeConnection(
            nodeContainer[i],
            targetNode,
            nodeContainer[i].outputConnector,
            targetNode.inputConnectors[content[1].substringBefore(')').toInt()]
        )
        nodeConnection.startConnector.nodeConnection = nodeConnection
        nodeConnection.endConnector.nodeConnection = nodeConnection
    }
    nodeContainer[0].invalidateInput(true, callback)
    return nodeContainer
}