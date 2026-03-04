import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    fun toSQL(jsonString: String): String {
        val jsonNode = stringToJsonNode(jsonString)

        return when (getOpType(jsonNode)) {
            OpType.INSERT -> toInsertSQL(jsonNode)
            OpType.UPDATE -> toUpdateSQL(jsonNode)
            OpType.DELETE -> TODO()
        }
    }

    private fun toInsertSQL(jsonNode: JsonNode): String {
        val table = getNamespace(jsonNode)
        val objectNode = getObjectNode(jsonNode)

        val columns = objectNode.fieldNames().asSequence().toList()
        val values = columns.map { formatValue(objectNode.get(it)) }

        return "INSERT INTO $table (${columns.joinToString()}) VALUES (${values.joinToString()});"
    }

    private fun toUpdateSQL(jsonNode: JsonNode): String {
        val table = getNamespace(jsonNode)
        val setClause = buildSetClause(jsonNode)
        val id = getId(jsonNode)

        return "UPDATE $table SET $setClause WHERE _id = '$id';"
    }

    private fun buildSetClause(jsonNode: JsonNode): String {
        val diff = getObjectNode(jsonNode).get("diff")

        diff.get("u")?.let { updates ->
            val column = updates.fieldNames().next()
            val value = formatValue(updates.get(column))
            return "$column = $value"
        }

        diff.get("d")?.let { deletes ->
            val column = deletes.fieldNames().next()
            return "$column = NULL"
        }

        throw IllegalArgumentException("Unsupported update operation")
    }

    private fun formatValue(value: JsonNode): String =
        when {
            value.isTextual -> "'${value.asText()}'"
            value.isBoolean -> value.asBoolean().toString()
            value.isNumber  -> value.numberValue().toString()
            else -> value.toString()
        }

    private fun getOpType(jsonNode: JsonNode): OpType =
        when (jsonNode.get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    private fun getNamespace(jsonNode: JsonNode): String =
        jsonNode.get("ns").asText()

    private fun getObjectNode(jsonNode: JsonNode): JsonNode =
        jsonNode.get("o")

    private fun getId(jsonNode: JsonNode): String =
        jsonNode.get("o2").get("_id").asText()

    private fun stringToJsonNode(jsonString: String): JsonNode =
        objectMapper.readTree(jsonString)
}
