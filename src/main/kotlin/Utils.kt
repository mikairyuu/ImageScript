import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.Mat
import java.awt.Color
import java.awt.image.BufferedImage


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
                    255 - col.getRed(),
                    255 - col.getGreen(),
                    255 - col.getBlue()
                )
                inputFile.setRGB(x, y, col.getRGB())
            }
        }
        return inputFile
    }

    fun newBrightness(source: BufferedImage, brightnessPercentage: Float): BufferedImage {

        val bi = BufferedImage(
            source.getWidth(null),
            source.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
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


fun toComposeImage(mat: Mat, converter: OpenCVFrameConverter.ToOrgOpenCvCoreMat): ImageBitmap {
    return Java2DFrameUtils.toBufferedImage(converter.convert(mat)).toComposeImageBitmap()
}


fun OpenCVFrameConverter.ToOrgOpenCvCoreMat.convertToOrgOpenCvCoreMat(img: ImageBitmap): Mat? {
    return convertToOrgOpenCvCoreMat(convert(Java2DFrameUtils.toMat((img).toAwtImage())))
}
