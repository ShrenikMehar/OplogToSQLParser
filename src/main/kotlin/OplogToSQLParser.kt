import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    fun read(json: String): JsonNode = objectMapper.readTree(json)

    fun getOpType(node: JsonNode): OpType =
        when (node.get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    fun getNamespace(node: JsonNode): String = node.get("ns").asText()

    fun toSQL(node: JsonNode): String =
        when (getOpType(node)) {
            OpType.INSERT -> toInsertSQL(node)
            OpType.UPDATE -> toUpdateSQL(node)
        }

    private fun toInsertSQL(node: JsonNode): String {
        val table = getNamespace(node)
        val objectNode = node.get("o")

        val columns = objectNode.fieldNames().asSequence().toList()
        val values = columns.map { field ->
            formatValue(objectNode.get(field))
        }

        return "INSERT INTO $table (${columns.joinToString()}) " +
                "VALUES (${values.joinToString()});"
    }

    private fun toUpdateSQL(node: JsonNode): String {
        val table = getNamespace(node)
        val diff = node.get("o").get("diff")

        val (column, value) =
            diff.get("u")?.let { updates ->
                val col = updates.fieldNames().next()
                col to formatValue(updates.get(col))
            }
                ?: diff.get("d")?.let { deletes ->
                    val col = deletes.fieldNames().next()
                    col to "NULL"
                }
                ?: throw IllegalArgumentException("Unsupported update operation")

        val id = node.get("o2").get("_id").asText()

        return "UPDATE $table SET $column = $value WHERE _id = '$id';"
    }

    private fun formatValue(value: JsonNode): String =
        when {
            value.isTextual -> "'${value.asText()}'"
            value.isBoolean -> value.asBoolean().toString()
            value.isNumber  -> value.numberValue().toString()
            else -> value.toString()
        }
}
