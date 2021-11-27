object NodeTypeStore {
    private val nodeTypeList = listOf(
        object : NodeType {
            override val id: Int = 0
            override val name: String = "Int"
            override val contentList: List<Any?> = listOf(0)
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 0
            override val output: Any = 0
        },
        object : NodeType {
            override val id: Int = 1
            override val name: String = "Float"
            override val contentList: List<Any?> = listOf(0f)
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 1
            override val output: Any = 0f
        },
        object : NodeType {
            override val id: Int = 2
            override val name: String = "String"
            override val contentList: List<Any?> = listOf("")
            override val inputNodeList: List<Int> = listOf()
            override val outputNode = 2
            override val output: Any = ""
        },
        object : NodeType {
            override val id: Int = 3
            override val name: String = "Brightness"
            override val contentList: List<Any?> = listOf()
            override val inputNodeList: List<Int> = listOf(1)
            override val outputNode = 3
            override val output: Any = 0
        }
    )

    fun getNode(id: Int): NodeType {
        return nodeTypeList[id]
    }

    fun getAllNodes(): List<NodeType> {
        return nodeTypeList
    }
}