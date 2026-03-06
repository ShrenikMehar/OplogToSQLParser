package parser.sql

import com.fasterxml.jackson.databind.JsonNode
import parser.json.OplogJsonAccessor

class InsertSQLBuilder(
    private val accessor: OplogJsonAccessor,
    private val sqlUtils: SqlUtils
) {

    fun build(node: JsonNode): String {
        val schemaSql = buildCreateSchema(node)
        val tableSql = buildCreateTable(node)
        val insertSql = buildInsert(node)

        return listOf(schemaSql, tableSql, insertSql).joinToString("\n\n")
    }

    private fun buildCreateSchema(node: JsonNode): String {
        val schema = accessor.getSchema(node)
        return "CREATE SCHEMA IF NOT EXISTS $schema;"
    }

    private fun buildCreateTable(node: JsonNode): String {
        val namespace = accessor.getNamespace(node)
        val objectNode = accessor.getObjectNode(node)

        val columns = accessor.getColumns(objectNode)
            .joinToString(", ") { buildColumnDefinition(it, objectNode.get(it)) }

        return "CREATE TABLE IF NOT EXISTS $namespace ($columns);"
    }

    fun buildInsert(node: JsonNode): String {
        val table = accessor.getNamespace(node)
        val objectNode = accessor.getObjectNode(node)

        val columns = accessor.getColumns(objectNode)
        val values = columns.map { sqlUtils.formatValue(objectNode.get(it)) }

        return "INSERT INTO $table (${columns.joinToString()}) VALUES (${values.joinToString()});"
    }

    private fun buildColumnDefinition(column: String, value: JsonNode): String {
        val type = sqlUtils.inferSqlType(value)
        return if (column == "_id") "$column $type PRIMARY KEY" else "$column $type"
    }
}
