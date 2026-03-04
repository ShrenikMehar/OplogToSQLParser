import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    private fun stringToJsonNode(jsonString: String): JsonNode
        = objectMapper.readTree(jsonString)

    private fun getOpType(jsonString: String): OpType =
        when (stringToJsonNode(jsonString).get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    private fun getNamespace(jsonString: String): String
        = stringToJsonNode(jsonString).get("ns").asText()

    fun toSQL(jsonString: String): String {
        return when (getOpType(jsonString)) {
            OpType.INSERT -> toInsertSQL(jsonString)
            OpType.UPDATE -> toUpdateSQL(jsonString)
        }
    }

    private fun toInsertSQL(jsonString: String): String {
        val table = getNamespace(jsonString)
        val node = stringToJsonNode(jsonString)
        val objectNode = node.get("o")

        val columns = objectNode.fieldNames().asSequence().toList()
        val values = columns.map { field ->
            formatValue(objectNode.get(field))
        }

        return "INSERT INTO $table (${columns.joinToString()}) " +
                "VALUES (${values.joinToString()});"
    }

    private fun toUpdateSQL(jsonString: String): String {
        val table = getNamespace(jsonString)
        val node = stringToJsonNode(jsonString)
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
