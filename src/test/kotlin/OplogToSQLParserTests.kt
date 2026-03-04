import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class OplogToSQLParserTests {
    private fun inputJson(): String {
        return javaClass
            .getResource("/oplog-insert.json")!!
            .readText()
    }

    private fun updateJsonForNewValue(): String {
        return javaClass
            .getResource("/oplog-update-new-value.json")!!
            .readText()
    }

    private fun updateJsonForUnsetting(): String {
        return javaClass
            .getResource("/oplog-update-unset.json")!!
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

        assertEquals(OpType.INSERT, opType)
    }

    @Test
    fun `should throw exception when op type is not supported`() {
        val parser = OplogToSQLParser()
        val invalidJson = """
            {
              "op": "x",
              "ns": "test.student",
              "o": {}
            }
        """
        val node = parser.read(invalidJson)

        assertFailsWith<IllegalArgumentException> {
            parser.getOpType(node)
        }
    }

    @Test
    fun `should extract namespace from oplog json`() {
        val parser = OplogToSQLParser()
        val node = parser.read(inputJson())

        val namespace = parser.getNamespace(node)

        assertEquals("test.student", namespace)
    }

    @Test
    fun `should generate insert sql from oplog json`() {
        val parser = OplogToSQLParser()
        val node = parser.read(inputJson())

        val sql = parser.toSQL(node)

        assertEquals(
            "INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) " +
                    "VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');",
            sql
        )
    }

    @Test
    fun `should generate update sql when setting a field`() {
        val parser = OplogToSQLParser()
        val node = parser.read(updateJsonForNewValue())

        val sql = parser.toSQL(node)

        assertEquals(
            "UPDATE test.student SET is_graduated = true WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should generate update sql when unsetting a field`() {
        val parser = OplogToSQLParser()
        val node = parser.read(updateJsonForUnsetting())

        val sql = parser.toSQL(node)

        assertEquals(
            "UPDATE test.student SET roll_no = NULL WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should throw exception when update operation is unsupported`() {
        val parser = OplogToSQLParser()
        val invalidJson = $$$"""
            {
                "op": "u",
                "ns": "test.student",
                "o": {
                    "$$v": 2,
                    "diff": {}
                },
                "o2": {
                    "_id": "635b79e231d82a8ab1de863b"
                }
            }
        """
        val node = parser.read(invalidJson)

        assertFailsWith<IllegalArgumentException> {
            parser.toSQL(node)
        }
    }
}
