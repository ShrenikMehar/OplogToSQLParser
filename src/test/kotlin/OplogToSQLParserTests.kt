import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OplogToSQLParserTests {
    private fun inputJson(): String {
        return javaClass
            .getResource("/oplog-insert.json")!!
            .readText()
    }

    private fun updateJsonForNewValue(): String {
        return javaClass
            .getResource("/oplog-update-set.json")!!
            .readText()
    }

    private fun updateJsonForUnsetting(): String {
        return javaClass
            .getResource("/oplog-update-unset.json")!!
            .readText()
    }

    private fun deleteJson(): String {
        return javaClass
            .getResource("/oplog-delete.json")!!
            .readText()
    }

    @Test
    fun `should generate insert sql from oplog json`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(inputJson())

        assertEquals(
            "INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) " +
                    "VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');",
            sql
        )
    }

    @Test
    fun `should generate update sql when setting a field`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(updateJsonForNewValue())

        assertEquals(
            "UPDATE test.student SET is_graduated = true WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should generate update sql when unsetting a field`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(updateJsonForUnsetting())

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
                    "$v": 2,
                    "diff": {}
                },
                "o2": {
                    "_id": "635b79e231d82a8ab1de863b"
                }
            }
        """

        assertFailsWith<IllegalArgumentException> {
            parser.toSQL(invalidJson)
        }
    }
}
