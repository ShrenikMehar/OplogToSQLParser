import org.junit.jupiter.api.Test
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
}
