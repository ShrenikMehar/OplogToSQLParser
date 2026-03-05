import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    fun toSQL(jsonString: String): String {
        val jsonNode = stringToJsonNode(jsonString)

        return when (getOpType(jsonNode)) {
            OpType.INSERT -> toInsertStatements(jsonNode)
            OpType.UPDATE -> toUpdateSQL(jsonNode)
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

    private fun toDeleteSQL(jsonNode: JsonNode): String {
        val table = getNamespace(jsonNode)
        val id = getObjectNode(jsonNode).get("_id").asText()

        return "DELETE FROM $table WHERE _id = '$id';"
    }

    private fun buildCreateSchema(node: JsonNode): String {
        val schema = getSchema(node)
        return "CREATE SCHEMA $schema;"
    }

    private fun buildCreateTable(node: JsonNode): String {
        val namespace = getNamespace(node)
        val objectNode = getObjectNode(node)

        val columns = objectNode.fieldNames().asSequence().joinToString(", ") {
            buildColumnDefinition(it, objectNode.get(it))
        }

        return "CREATE TABLE $namespace ($columns);"
    }

    private fun buildColumnDefinition(column: String, value: JsonNode): String {
        val type = inferSqlType(value)
        return if (column == "_id") "$column $type PRIMARY KEY" else "$column $type"
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

    private fun inferSqlType(value: JsonNode): String = when {
        value.isTextual -> "VARCHAR(255)"
        value.isBoolean -> "BOOLEAN"
        value.isNumber -> "FLOAT"
        else -> "VARCHAR(255)"
    }

    private fun getOpType(jsonNode: JsonNode): OpType =
        when (jsonNode.get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            "d" -> OpType.DELETE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    private fun getNamespace(jsonNode: JsonNode): String =
        jsonNode.get("ns").asText()

    private fun getSchema(node: JsonNode): String =
        getNamespace(node).split(".")[0]

    private fun getTable(node: JsonNode): String =
        getNamespace(node).split(".")[1]

    private fun getObjectNode(jsonNode: JsonNode): JsonNode =
        jsonNode.get("o")

    private fun getId(jsonNode: JsonNode): String =
        jsonNode.get("o2").get("_id").asText()

    private fun stringToJsonNode(jsonString: String): JsonNode =
        objectMapper.readTree(jsonString)
}
