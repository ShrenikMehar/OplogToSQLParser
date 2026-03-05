class OplogToSQLParser {

    private val jsonParser = OplogJsonParser()
    private val jsonAccessor = OplogJsonAccessor()
    private val sqlUtils = SqlUtils()
    private val insertBuilder = InsertSQLBuilder(jsonAccessor, sqlUtils)
    private val updateBuilder = UpdateSQLBuilder(jsonAccessor, sqlUtils)
    private val deleteBuilder = DeleteSQLBuilder(jsonAccessor)

    fun toSQL(jsonString: String): String {
        val jsonNode = jsonParser.parse(jsonString)

        return when (jsonAccessor.getOpType(jsonNode)) {
            OpType.INSERT -> insertBuilder.build(jsonNode)
            OpType.UPDATE -> updateBuilder.build(jsonNode)
            OpType.DELETE -> deleteBuilder.build(jsonNode)
        }
    }
}
