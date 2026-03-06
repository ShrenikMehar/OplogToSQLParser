package parser

import com.fasterxml.jackson.databind.JsonNode
import parser.json.OplogJsonAccessor
import parser.json.OplogJsonParser
import parser.model.OpType
import parser.sql.DeleteSQLBuilder
import parser.sql.InsertSQLBuilder
import parser.sql.SqlUtils
import parser.sql.UpdateSQLBuilder

class OplogToSQLParser {

    private val jsonParser = OplogJsonParser()
    private val jsonAccessor = OplogJsonAccessor()
    private val sqlUtils = SqlUtils()
    private val insertBuilder = InsertSQLBuilder(jsonAccessor, sqlUtils)
    private val updateBuilder = UpdateSQLBuilder(jsonAccessor, sqlUtils)
    private val deleteBuilder = DeleteSQLBuilder(jsonAccessor)

    fun toSQL(jsonString: String): String {
        val jsonNode = jsonParser.parse(jsonString)

        return when {
            jsonNode.isArray -> processMultiple(jsonNode)
            else -> processSingle(jsonNode)
        }
    }

    private fun processSingle(jsonNode: JsonNode): String =
        when (jsonAccessor.getOpType(jsonNode)) {
            OpType.INSERT -> insertBuilder.build(jsonNode)
            OpType.UPDATE -> updateBuilder.build(jsonNode)
            OpType.DELETE -> deleteBuilder.build(jsonNode)
        }

    private fun processMultiple(arrayNode: JsonNode): String {
        val nodes = arrayNode.toList()
        val first = nodes.first()

        val schemaTableAndFirstInsert = insertBuilder.build(first)
        val otherInserts = nodes.drop(1).map { insertBuilder.buildInsert(it) }

        return listOf(schemaTableAndFirstInsert, *otherInserts.toTypedArray())
            .joinToString("\n\n")
    }
}