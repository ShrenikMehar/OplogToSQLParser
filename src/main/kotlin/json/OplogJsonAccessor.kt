package json

import com.fasterxml.jackson.databind.JsonNode
import model.OpType

class OplogJsonAccessor {

    fun getOpType(node: JsonNode): OpType =
        when (node.get("op")?.asText()) {
            "i" -> OpType.INSERT
            "u" -> OpType.UPDATE
            "d" -> OpType.DELETE
            else -> throw IllegalArgumentException("Operation Type is not supported")
        }

    fun getNamespace(node: JsonNode): String =
        node.get("ns").asText()

    fun getSchema(node: JsonNode): String =
        getNamespace(node).substringBefore(".")

    fun getTable(node: JsonNode): String =
        getNamespace(node).substringAfter(".")

    fun getColumns(objectNode: JsonNode): List<String> =
        objectNode.fieldNames().asSequence().toList()

    fun getObjectNode(node: JsonNode): JsonNode =
        node.get("o")

    fun getId(node: JsonNode): String =
        node.get("o2").get("_id").asText()
}
