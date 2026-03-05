import com.fasterxml.jackson.databind.JsonNode

class SqlUtils {

    fun formatValue(value: JsonNode): String =
        when {
            value.isTextual -> "'${value.asText()}'"
            value.isBoolean -> value.asBoolean().toString()
            value.isNumber -> value.numberValue().toString()
            else -> value.toString()
        }
}
