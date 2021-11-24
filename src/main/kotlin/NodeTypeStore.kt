object NodeTypeStore {
    private val nodeTypeList = listOf(
        object : NodeType {
            override val name: String = "Int"
            override val contentList: List<Any?> = listOf(0)
            override val inputList: List<NodeType> = listOf()
            override val output: Any = 0
        },
        object : NodeType {
            override val name: String = "Float"
            override val contentList: List<Any?> = listOf(0f)
            override val inputList: List<NodeType> = listOf()
            override val output: Any = 0f
        },
        object : NodeType {
            override val name: String = "String"
            override val contentList: List<Any?> = listOf("")
            override val inputList: List<NodeType> = listOf()
            override val output: Any = ""
        }
    )

    fun getNode(id: Int): NodeType {
        return nodeTypeList[id]
    }

    fun getAllNodes(): List<NodeType> {
        return nodeTypeList
    }
}