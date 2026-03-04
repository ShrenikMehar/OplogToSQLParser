import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    private fun stringToJsonNode(jsonString: String): JsonNode
        = objectMapper.readTree(jsonString)

    private fun getOpType(jsonNode: JsonNode): OpType =
        when (jsonNode.get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    private fun getNamespace(jsonNode: JsonNode): String
        = jsonNode.get("ns").asText()

    private fun getObjectNode(jsonNode: JsonNode): JsonNode
            = jsonNode.get("o")

    fun toSQL(jsonString: String): String {
        val jsonNode = stringToJsonNode(jsonString)

        return when (getOpType(jsonNode)) {
            OpType.INSERT -> toInsertSQL(jsonNode)
            OpType.UPDATE -> toUpdateSQL(jsonNode)
        }
    }

    private fun toInsertSQL(jsonNode: JsonNode): String {
        val table = getNamespace(jsonNode)
        val objectNode = getObjectNode(jsonNode)

        val columns = objectNode.fieldNames().asSequence().toList()
        val values = columns.map { field ->
            formatValue(objectNode.get(field))
        }

        return "INSERT INTO $table (${columns.joinToString()}) " +
                "VALUES (${values.joinToString()});"
    }

    private fun toUpdateSQL(jsonNode: JsonNode): String {
        val table = getNamespace(jsonNode)
        val diff = getObjectNode(jsonNode).get("diff")

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

        val id = jsonNode.get("o2").get("_id").asText()

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
