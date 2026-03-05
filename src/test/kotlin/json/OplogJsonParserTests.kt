package json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OplogJsonParserTests {

    private val parser = OplogJsonParser()

    @Test
    fun `should parse json string into JsonNode`() {
        val json = """
            {
              "op": "i",
              "ns": "test.student",
              "o": {
                "_id": "1",
                "name": "Selena"
              }
            }
        """.trimIndent()

        val node = parser.parse(json)

        assertNotNull(node)
        assertEquals("i", node.get("op").asText())
        assertEquals("test.student", node.get("ns").asText())
    }
}
