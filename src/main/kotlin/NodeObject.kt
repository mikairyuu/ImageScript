import java.awt.Dimension

interface NodeType {
    val name: String
    val contentList: List<Any?>
    val inputList: List<NodeType>
    val output: Any?
}

class NodeObject(
    val nodeType: NodeType,
    var Xpos: Int,
    var Ypos: Int
) {
    //creating with the pos in the center
    constructor(nodeType: NodeType, layoutSize: Dimension) : this(nodeType, layoutSize.width / 2, layoutSize.height / 2)

    var content: MutableList<Any?> = nodeType.contentList.toMutableList()
    var input: MutableList<Any?> = nodeType.inputList.toMutableList()
    var output: Any? = nodeType.output
}