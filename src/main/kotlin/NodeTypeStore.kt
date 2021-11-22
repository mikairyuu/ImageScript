object NodeTypeStore {
    private val nodeArray = listOf(
        object : NodeType {
            override val name: String = "Int"
        },
        object : NodeType {
            override val name: String = "Float"
        },
        object : NodeType {
            override val name: String = "String"
        }

    )

    fun getNode(id: Int): NodeType {
        return nodeArray[id]
    }

    fun getAllNodes(): List<NodeType> {
        return nodeArray
    }
}