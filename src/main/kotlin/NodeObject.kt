import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import java.awt.Dimension
import kotlin.math.absoluteValue

abstract class NodeType {
    abstract val id: Int
    abstract val name: String
    abstract val contentList: List<Any?>
    abstract val inputNodeList: List<Int>
    abstract val inputNameList: List<String>
    abstract val outputNode: Int
    abstract val outputFun: (contentList: List<Any?>, inputList: List<Any?>) -> Any?
    open val isInList: Boolean = true
    open val miscAttribute: Any? = null
}

class NodeObject(
    val nodeType: NodeType,
    var Xpos: Int,
    var Ypos: Int
) {
    //creating with the pos in the center
    constructor(nodeType: NodeType, layoutSize: Dimension) : this(nodeType, layoutSize.width / 2, layoutSize.height / 2)

    var content: MutableList<Any?> = nodeType.contentList.toMutableList()
    var input: MutableList<Int> = nodeType.inputNodeList.toMutableList()
    var output: Any? = null
    var outputConnector: NodeConnector =
        NodeConnector(Offset(0f, 0f), null, false, NodeTypeStore.getNode(nodeType.outputNode))
    var inputConnectors: List<NodeConnector>
    var isError = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    init {
        val inputConnectorsList = mutableListOf<NodeConnector>()
        for (i in input.indices) {
            inputConnectorsList.add(
                NodeConnector(
                    Offset(0f, 0f),
                    null,
                    true,
                    NodeTypeStore.getNode(nodeType.inputNodeList[i])
                )
            )
        }
        inputConnectors = inputConnectorsList
        invalidateInput()
    }

    fun invalidateInput(forceTraverse: Boolean = false) {
        job?.cancel()
        job = coroutineScope.launch {
            try {
                val pastOutput = output
                output = nodeType.outputFun(content, getInputList())
                if (pastOutput != output || forceTraverse)
                    outputConnector.nodeConnection?.endNodeObject?.invalidateInput(false)
                isError = false
            } catch (e: Exception) {
                e.printStackTrace()
                output = null
                isError = true
            }
        }
    }

    private fun getInputList(): List<Any?> {
        val r = mutableListOf<Any?>()
        inputConnectors.forEach {
            val curOutput = it.nodeConnection?.startNodeObject?.output
            if (curOutput != null) r.add(curOutput)
        }
        return r
    }

    fun destroy() {
        coroutineScope.cancel()
    }

}

data class NodeConnection(
    var startNodeObject: NodeObject,
    var endNodeObject: NodeObject?,
    var startConnector: NodeConnector,
    var endConnector: NodeConnector
)

data class NodeConnector(
    var offset: Offset,
    var nodeConnection: NodeConnection?,
    val isInput: Boolean,
    val transferNodeType: NodeType
)

fun NodeConnector.Remove() {
    if (nodeConnection != null) {
        if (nodeConnection!!.endConnector == this) {
            nodeConnection!!.startConnector.nodeConnection = null
            nodeConnection!!.endConnector.nodeConnection = null
        } else {
            nodeConnection!!.endConnector.nodeConnection = null
            nodeConnection!!.endNodeObject?.invalidateInput(false)
            nodeConnection!!.startConnector.nodeConnection = null
        }
    }
}

fun Offset.isInBounds(nodeConnector: NodeConnector): Boolean {
    val res = (nodeConnector.offset.getDistanceSquared() - this.getDistanceSquared()).absoluteValue
    return res < 5000
}