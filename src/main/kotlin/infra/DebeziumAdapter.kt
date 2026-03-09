package infra

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

class DebeziumAdapter {

    private val mapper = ObjectMapper()

    fun normalize(event: String): String {

        val root = mapper.readTree(event)
        val payload = root.get("payload") ?: return event

        val op = payload.get("op").asText()
        val db = payload.get("source").get("db").asText()
        val collection = payload.get("source").get("collection").asText()

        val namespace = "$db.$collection"

        return when (op) {
            "c" -> buildInsert(payload, namespace)
            "u" -> buildUpdate(payload, namespace)
            "d" -> buildDelete(payload, namespace)
            else -> event
        }
    }

    private fun buildInsert(payload: JsonNode, ns: String): String {

        val doc = mapper.readTree(payload.get("after").asText())

        val result: ObjectNode = mapper.createObjectNode()
        result.put("op", "i")
        result.put("ns", ns)
        result.set<ObjectNode>("o", doc)

        return mapper.writeValueAsString(result)
    }

    private fun buildUpdate(payload: JsonNode, ns: String): String {

        val updatedFields = mapper.readTree(
            payload.get("updateDescription").get("updatedFields").asText()
        )

        val afterDoc = mapper.readTree(payload.get("after").asText())
        val id = afterDoc.get("_id")

        val diff = mapper.createObjectNode()
        diff.set<ObjectNode>("u", updatedFields)

        val o = mapper.createObjectNode()
        o.put("\$v", 2)
        o.set<ObjectNode>("diff", diff)

        val result: ObjectNode = mapper.createObjectNode()
        result.put("op", "u")
        result.put("ns", ns)
        result.set<ObjectNode>("o", o)

        val o2 = mapper.createObjectNode()
        o2.set<ObjectNode>("_id", id)

        result.set<ObjectNode>("o2", o2)

        return mapper.writeValueAsString(result)
    }

    private fun buildDelete(payload: JsonNode, ns: String): String {

        val before = mapper.readTree(payload.get("before").asText())

        val result: ObjectNode = mapper.createObjectNode()
        result.put("op", "d")
        result.put("ns", ns)
        result.set<ObjectNode>("o", before)

        return mapper.writeValueAsString(result)
    }
}
