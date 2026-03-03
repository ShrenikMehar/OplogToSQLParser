import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    fun read(json: String): JsonNode = objectMapper.readTree(json)

    fun getOpType(node: JsonNode): OpType =
        when (node.get("op")?.asText()) {
            "i" -> OpType.INSERT
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    fun getNamespace(node: JsonNode): String = node.get("ns").asText()

    fun toSQL(node: JsonNode): String =
        when (getOpType(node)) {
            OpType.INSERT -> toInsertSQL(node)
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

    private fun formatValue(value: JsonNode): String =
        when {
            value.isTextual -> "'${value.asText()}'"
            value.isBoolean -> value.asBoolean().toString()
            value.isNumber  -> value.numberValue().toString()
            else -> value.toString()
        }
}
