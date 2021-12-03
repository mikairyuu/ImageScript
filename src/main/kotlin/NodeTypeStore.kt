import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


enum class ImageView {
    Viewable,
    Saveable,
    Openable
}

object NodeTypeStore {
    private val nodeTypeList = listOf(
        object : NodeType() {
            override val id: Int = 0
            override val name: String = "Int"
            override val contentList: List<Any?> = listOf(0)
            override val inputNodeList: List<Int> = listOf()
            override val inputNameList: List<String> = listOf()
            override val outputNode = 0
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 1
            override val name: String = "Float"
            override val contentList: List<Any?> = listOf(0f)
            override val inputNodeList: List<Int> = listOf()
            override val inputNameList: List<String> = listOf()
            override val outputNode = 1
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 2
            override val name: String = "String"
            override val contentList: List<Any?> = listOf("")
            override val inputNodeList: List<Int> = listOf()
            override val inputNameList: List<String> = listOf()
            override val outputNode = 2
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 3
            override val name: String = "Image"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf()
            override val inputNameList: List<String> = listOf()
            override val outputNode = 3
            override val outputFun =
                { contentList: List<Any?>, _: List<Any?> -> if (contentList[0] != null) contentList[0] else throw Exception() }
            override val miscAttribute: Any = ImageView.Openable
        },
        object : NodeType() {
            override val id: Int = 4
            override val name: String = "Входное изображение"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf()
            override val inputNameList: List<String> = listOf()
            override val outputNode = 3
            override val isInList = false
            override val outputFun =
                { contentList: List<Any?>, _: List<Any?> -> if (contentList[0] != null) contentList[0] else throw Exception() }
            override val miscAttribute: Any = ImageView.Openable
        },
        object : NodeType() {
            override val id: Int = 5
            override val name: String = "Конечное изображение"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3)
            override val inputNameList: List<String> = listOf("img")
            override val outputNode = 0
            override val isInList = false
            override val outputFun = { _: List<Any?>, inputList: List<Any?> -> inputList[0] }
            override val miscAttribute: Any = ImageView.Saveable
        },
        object : NodeType() {
            override val id: Int = 6
            override val name: String = "Добавить текст"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 0, 0, 2)
            override val inputNameList: List<String> = listOf("img", "x", "y", "text")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val src = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    Imgproc.putText(
                        src,
                        inputList[3] as String,
                        Point((inputList[1] as Int).toDouble(), (inputList[2] as Int).toDouble()),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        10.0,
                        Scalar(255.0, 255.0, 255.0),
                        5
                    )
                    toComposeImage(src, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 7
            override val name: String = "Добавить изображение"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 0, 0, 3)
            override val inputNameList: List<String> = listOf("img", "x", "y", "addImg")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val source = (inputList[0] as ImageBitmap).toAwtImage()
                    source.graphics.drawImage(
                        (inputList[3] as ImageBitmap).toAwtImage(),
                        inputList[1] as Int,
                        inputList[2] as Int,
                        null
                    )
                    source.toComposeImageBitmap()
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 8
            override val name: String = "Gray filter"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3)
            override val inputNameList: List<String> = listOf("img")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val mat = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
                    toComposeImage(mat, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 9
            override val name: String = "Brightness"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 1)
            override val inputNameList: List<String> = listOf("img", "bright")
            override val outputNode = 3
            override val outputFun: (List<Any?>, List<Any?>) -> Any? =
                { _: List<Any?>, inputList: List<Any?> ->
                    val img = (inputList[0] as ImageBitmap).toAwtImage()
                    Utils.newBrightness(img, inputList[1] as Float).toComposeImageBitmap()
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 10
            override val name: String = "Sepia"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3)
            override val inputNameList: List<String> = listOf("img")
            override val outputNode = 3
            override val outputFun: (List<Any?>, List<Any?>) -> Any? =
                { _: List<Any?>, inputList: List<Any?> ->
                    Utils.toSepia((inputList[0] as ImageBitmap).toAwtImage()).toComposeImageBitmap()
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 11
            override val name: String = "Invert Filter"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3)
            override val inputNameList: List<String> = listOf("img")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    Utils.invertImage((inputList[0] as ImageBitmap).toAwtImage()).toComposeImageBitmap()
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 12
            override val name: String = "Blur Filter"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 0)
            override val inputNameList: List<String> = listOf("img", "kernelSize")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val mat = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    var size = (inputList[1] as Int).toDouble()
                    if ((size % 2) == 0.0) size += 1
                    Imgproc.GaussianBlur(mat, mat, Size(size, size), 0.0)
                    toComposeImage(mat, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 13
            override val name: String = "Transform Move"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 1, 1)
            override val inputNameList: List<String> = listOf("img", "x", "y")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val mat = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    val transMat = Mat(2, 3, CvType.CV_64FC1)
                    transMat.put(
                        0,
                        0,
                        1.0,
                        0.0,
                        (inputList[1] as Float).toDouble(),
                        0.0,
                        1.0,
                        (inputList[2] as Float).toDouble()
                    )
                    Imgproc.warpAffine(mat, mat, transMat, mat.size());
                    toComposeImage(mat, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 14
            override val name: String = "Transform Scale"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 1, 1)
            override val inputNameList: List<String> = listOf("img", "x", "y")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val mat = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    Imgproc.resize(
                        mat, mat, Size((inputList[1] as Float).toDouble(), (inputList[2] as Float).toDouble())
                    )
                    toComposeImage(mat, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
        object : NodeType() {
            override val id: Int = 15
            override val name: String = "Transform Rotate"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf(3, 1)
            override val inputNameList: List<String> = listOf("img", "rad")
            override val outputNode = 3
            override val outputFun =
                { _: List<Any?>, inputList: List<Any?> ->
                    val conv = OpenCVFrameConverter.ToOrgOpenCvCoreMat()
                    val mat = convertToOrgOpenCvCoreMat(inputList[0] as ImageBitmap)
                    val rotMat = Imgproc.getRotationMatrix2D(
                        Point(mat.cols() / 2.0, mat.rows() / 2.0),
                        Math.toDegrees((inputList[1] as Float).toDouble()),
                        1.0
                    )
                    Imgproc.warpAffine(mat, mat, rotMat, mat.size());
                    toComposeImage(mat, conv)
                }
            override val miscAttribute: Any = ImageView.Viewable
        },
    )

    fun getNodeType(id: Int): NodeType {
        return nodeTypeList[id]
    }

    fun getAllNodes(): List<NodeType> {
        return nodeTypeList
    }
}