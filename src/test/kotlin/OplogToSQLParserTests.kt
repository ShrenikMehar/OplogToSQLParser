import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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

        assertTrue(
            sql.contains(
                "INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) " +
                        "VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');"
            )
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

    @Test
    fun `should generate update sql when deleting an entry`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(deleteJson())

        assertEquals(
            "DELETE FROM test.student WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should generate create schema statement`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(inputJson())

        assertTrue(sql.contains("CREATE SCHEMA test;"))
    }

    @Test
    fun `should infer correct sql column types`() {
        val parser = OplogToSQLParser()
        val json = inputJson()

        val sql = parser.toSQL(json)

        assertTrue(sql.contains("_id VARCHAR(255) PRIMARY KEY"))
        assertTrue(sql.contains("name VARCHAR(255)"))
        assertTrue(sql.contains("roll_no FLOAT"))
        assertTrue(sql.contains("is_graduated BOOLEAN"))
    }

    @Test
    fun `should generate create schema, create table and insert statements`() {
        val parser = OplogToSQLParser()

        val sql = parser.toSQL(inputJson())

        val expected = """
        CREATE SCHEMA test;

        CREATE TABLE test.student (_id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), roll_no FLOAT, is_graduated BOOLEAN, date_of_birth VARCHAR(255));

        INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');
    """.trimIndent()

        assertEquals(expected, sql)
    }
}
