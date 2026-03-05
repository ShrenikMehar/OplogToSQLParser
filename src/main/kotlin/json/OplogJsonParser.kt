package json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogJsonParser {

    private val objectMapper = jacksonObjectMapper()

    fun parse(json: String): JsonNode = objectMapper.readTree(json)
}
