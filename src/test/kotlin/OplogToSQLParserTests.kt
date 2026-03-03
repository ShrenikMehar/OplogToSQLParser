import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class OplogToSQLParserTests {
    
    @Test
    fun `should return output JSON as is`() {
        val inputJson = javaClass
            .getResource("/oplog-insert.json")!!
            .readText()

        val parser = OplogToSQLParser()
        val outputJson = parser.read(inputJson)
        
        assertEquals(inputJson, outputJson)
    }
}
