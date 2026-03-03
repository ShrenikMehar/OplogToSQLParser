import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class OplogToSQLParser {

    private val objectMapper = jacksonObjectMapper()

    fun read(json: String): JsonNode = objectMapper.readTree(json)
}
