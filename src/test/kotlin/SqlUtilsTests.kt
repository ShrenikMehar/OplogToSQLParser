import kotlin.test.Test
import kotlin.test.assertEquals

class SqlUtilsTests {

    private val parser = OplogJsonParser()
    private val sqlUtils = SqlUtils()

    @Test
    fun `should format string values with quotes`() {
        val node = parser.parse("\"Selena\"")

        val result = sqlUtils.formatValue(node)

        assertEquals("'Selena'", result)
    }

    @Test
    fun `should format boolean values`() {
        val node = parser.parse("true")

        val result = sqlUtils.formatValue(node)

        assertEquals("true", result)
    }

    @Test
    fun `should format numeric values`() {
        val node = parser.parse("51")

        val result = sqlUtils.formatValue(node)

        assertEquals("51", result)
    }

    @Test
    fun `should fallback to default toString for unknown types`() {
        val node = parser.parse("{}")

        val result = sqlUtils.formatValue(node)

        assertEquals("{}", result)
    }

    @Test
    fun `should infer varchar type for text`() {
        val node = parser.parse("\"hello\"")

        val type = sqlUtils.inferSqlType(node)

        assertEquals("VARCHAR(255)", type)
    }

    @Test
    fun `should infer boolean type`() {
        val node = parser.parse("true")

        val type = sqlUtils.inferSqlType(node)

        assertEquals("BOOLEAN", type)
    }

    @Test
    fun `should infer float type for number`() {
        val node = parser.parse("51")

        val type = sqlUtils.inferSqlType(node)

        assertEquals("FLOAT", type)
    }
}
