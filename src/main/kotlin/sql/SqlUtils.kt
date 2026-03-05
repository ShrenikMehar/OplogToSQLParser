package sql

import com.fasterxml.jackson.databind.JsonNode

class SqlUtils {

    fun formatValue(value: JsonNode): String =
        when {
            value.isTextual -> "'${value.asText()}'"
            value.isBoolean -> value.asBoolean().toString()
            value.isNumber -> value.numberValue().toString()
            else -> value.toString()
        }

    fun inferSqlType(value: JsonNode): String =
        when {
            value.isTextual -> "VARCHAR(255)"
            value.isBoolean -> "BOOLEAN"
            value.isNumber -> "FLOAT"
            else -> "VARCHAR(255)"
        }
}
