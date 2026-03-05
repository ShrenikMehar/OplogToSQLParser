import kotlin.test.Test
import kotlin.test.assertEquals

class OplogJsonAccessorTests {

    private val accessor = OplogJsonAccessor()

    private val json = """
        {
          "op": "i",
          "ns": "test.student",
          "o": {
            "_id": "123",
            "name": "Selena"
          },
          "o2": {
            "_id": "123"
          }
        }
    """.trimIndent()

    private val node = OplogJsonParser().parse(json)

    @Test
    fun `should extract operation type`() {
        val opType = accessor.getOpType(node)
        assertEquals(OpType.INSERT, opType)
    }

    @Test
    fun `should extract namespace`() {
        val namespace = accessor.getNamespace(node)
        assertEquals("test.student", namespace)
    }

    @Test
    fun `should extract schema`() {
        val schema = accessor.getSchema(node)
        assertEquals("test", schema)
    }

    @Test
    fun `should extract table`() {
        val table = accessor.getTable(node)
        assertEquals("student", table)
    }

    @Test
    fun `should extract columns from object node`() {
        val objectNode = accessor.getObjectNode(node)
        val columns = accessor.getColumns(objectNode)

        assertEquals(listOf("_id", "name"), columns)
    }

    @Test
    fun `should extract object node`() {
        val objectNode = accessor.getObjectNode(node)
        assertEquals("Selena", objectNode.get("name").asText())
    }

    @Test
    fun `should extract id`() {
        val id = accessor.getId(node)
        assertEquals("123", id)
    }
}
