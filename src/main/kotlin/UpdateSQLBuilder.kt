import com.fasterxml.jackson.databind.JsonNode

class UpdateSQLBuilder(
    private val accessor: OplogJsonAccessor,
    private val sqlUtils: SqlUtils
) {

    fun build(node: JsonNode): String {
        val table = accessor.getNamespace(node)
        val setClause = buildSetClause(node)
        val id = accessor.getId(node)

        return "UPDATE $table SET $setClause WHERE _id = '$id';"
    }

    private fun buildSetClause(node: JsonNode): String {
        val objectNode = accessor.getObjectNode(node)
        val diff = objectNode.get("diff")

        diff.get("u")?.let { updates ->
            val column = updates.fieldNames().next()
            val value = sqlUtils.formatValue(updates.get(column))
            return "$column = $value"
        }

        diff.get("d")?.let { deletes ->
            val column = deletes.fieldNames().next()
            return "$column = NULL"
        }

        throw IllegalArgumentException("Unsupported update operation")
    }
}
