import com.fasterxml.jackson.databind.JsonNode

class OplogToSQLParser {

    private val jsonParser = OplogJsonParser()
    private val jsonAccessor = OplogJsonAccessor()
    private val sqlUtils = SqlUtils()
    private val updateBuilder = UpdateSQLBuilder(jsonAccessor, sqlUtils)

    fun toSQL(jsonString: String): String {
        val jsonNode = jsonParser.parse(jsonString)

        return when (jsonAccessor.getOpType(jsonNode)) {
            OpType.INSERT -> toInsertStatements(jsonNode)
            OpType.UPDATE -> updateBuilder.build(jsonNode)
            OpType.DELETE -> toDeleteSQL(jsonNode)
        }
    }

    private fun toInsertStatements(node: JsonNode): String {
        val schemaSql = buildCreateSchema(node)
        val tableSql = buildCreateTable(node)
        val insertSql = toInsertSQL(node)

        return listOf(schemaSql, tableSql, insertSql).joinToString("\n\n")
    }

    private fun toInsertSQL(jsonNode: JsonNode): String {
        val table = jsonAccessor.getNamespace(jsonNode)
        val objectNode = jsonAccessor.getObjectNode(jsonNode)

        val columns = jsonAccessor.getColumns(objectNode)
        val values = columns.map { sqlUtils.formatValue(objectNode.get(it)) }

        return "INSERT INTO $table (${columns.joinToString()}) VALUES (${values.joinToString()});"
    }

    private fun toDeleteSQL(jsonNode: JsonNode): String {
        val table = jsonAccessor.getNamespace(jsonNode)
        val id = jsonAccessor.getObjectNode(jsonNode).get("_id").asText()

        return "DELETE FROM $table WHERE _id = '$id';"
    }

    private fun buildCreateSchema(node: JsonNode): String {
        val schema = jsonAccessor.getSchema(node)
        return "CREATE SCHEMA $schema;"
    }

    private fun buildCreateTable(node: JsonNode): String {
        val namespace = jsonAccessor.getNamespace(node)
        val objectNode = jsonAccessor.getObjectNode(node)

        val columns = jsonAccessor.getColumns(objectNode).joinToString(", ") {
            buildColumnDefinition(it, objectNode.get(it))
        }

        return "CREATE TABLE $namespace ($columns);"
    }

    private fun buildColumnDefinition(column: String, value: JsonNode): String {
        val type = inferSqlType(value)
        return if (column == "_id") "$column $type PRIMARY KEY" else "$column $type"
    }

    private fun inferSqlType(value: JsonNode): String = when {
        value.isTextual -> "VARCHAR(255)"
        value.isBoolean -> "BOOLEAN"
        value.isNumber -> "FLOAT"
        else -> "VARCHAR(255)"
    }
}
