package parser

import com.fasterxml.jackson.databind.JsonNode
import sql.DeleteSQLBuilder
import sql.InsertSQLBuilder
import json.OplogJsonAccessor
import json.OplogJsonParser
import model.OpType
import sql.SqlUtils
import sql.UpdateSQLBuilder

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

    private fun processMultiple(jsonArrayNode: JsonNode): String {
        return jsonArrayNode.joinToString("\n\n") { processSingle(it) }
    }
}
