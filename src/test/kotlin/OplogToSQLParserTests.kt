import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OplogToSQLParserTests {
    private fun inputJson(): String {
        return javaClass
            .getResource("/oplog-insert.json")!!
            .readText()
    }

    @Test
    fun `should parse input json into JsonNode`() {
        val parser = OplogToSQLParser()
        val node = parser.read(inputJson())

        assertNotNull(node)
    }

    @Test
    fun `should extract op type from oplog json`() {
        val parser = OplogToSQLParser()
        val node = parser.read(inputJson())

        val opType = parser.getOpType(node)

        assertEquals("i", opType)
    }
}
