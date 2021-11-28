import androidx.compose.ui.graphics.ImageBitmap
import java.lang.Exception

object NodeTypeStore {
    private val nodeTypeList = listOf(
        object : NodeType() {
            override val id: Int = 0
            override val name: String = "Int"
            override val contentList: List<Any?> = listOf(0)
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 0
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 1
            override val name: String = "Float"
            override val contentList: List<Any?> = listOf(0f)
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 1
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 2
            override val name: String = "String"
            override val contentList: List<Any?> = listOf("")
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 2
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> contentList[0] }
        },
        object : NodeType() {
            override val id: Int = 3
            override val name: String = "Image"
            override val contentList: List<Any?> = listOf(ImageBitmap(1, 1))
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 3
            override val canOpenImages: Boolean = true
            override val outputFun =
                { contentList: List<Any?>, _: List<Any?> -> if (contentList[0] != null) contentList[0] else throw Exception() }
        },
        object : NodeType() {
            override val id: Int = 4
            override val name: String = "Brightness"
            override val contentList: List<Any?> = listOf()
            override val inputNodeList: List<Int> = listOf(3, 1)
            override val outputNode = 3
            override val outputFun: (List<Any?>, List<Any?>) -> Any? =
                { contentList: List<Any?>, inputList: List<Any?> -> }
        },
        object : NodeType() {
            override val id: Int = 5
            override val name: String = "Image Input"
            override val contentList: List<Any?> = listOf(null as ImageBitmap?)
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 3
            override val isInList = false
            override val canOpenImages: Boolean = true
            override val outputFun = { contentList: List<Any?>, _: List<Any?> -> if (contentList[0] != null) contentList[0] else throw Exception() }
        },
        object : NodeType() {
            override val id: Int = 6
            override val name: String = "Image Output"
            override val contentList: List<Any?> = listOf(ImageBitmap(1, 1))
            override val inputNodeList: List<Int> = listOf(3)
            override val outputNode = 0
            override val isInList = false
            override val outputFun = { _: List<Any?>, inputList: List<Any?> -> inputList[0] }
            override val canOpenImages = false
        }
    )

    fun getNode(id: Int): NodeType {
        return nodeTypeList[id]
    }

    fun getAllNodes(): List<NodeType> {
        return nodeTypeList
    }
}