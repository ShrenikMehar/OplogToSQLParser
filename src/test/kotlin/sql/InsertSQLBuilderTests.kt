package sql

import json.OplogJsonAccessor
import json.OplogJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class InsertSQLBuilderTests {

    private val parser = OplogJsonParser()
    private val accessor = OplogJsonAccessor()
    private val sqlUtils = SqlUtils()

    private val builder = InsertSQLBuilder(accessor, sqlUtils)
    private val node = parser.parse(inputJson())

    private fun inputJson(): String {
        return javaClass
            .getResource("/oplog-insert.json")!!
            .readText()
    }

    @Test
    fun `should generate only insert statement`() {
        val sql = builder.buildInsert(node)

        val expected =
            "INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');"

        assertEquals(expected, sql)
    }

    @Test
    fun `should generate create schema create table and insert sql`() {
        val sql = builder.build(node)

        val expected = """
CREATE SCHEMA IF NOT EXISTS test;

CREATE TABLE IF NOT EXISTS test.student (_id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), roll_no FLOAT, is_graduated BOOLEAN, date_of_birth VARCHAR(255));

INSERT INTO test.student (_id, name, roll_no, is_graduated, date_of_birth) VALUES ('635b79e231d82a8ab1de863b', 'Selena Miller', 51, false, '2000-01-30');
""".trimIndent()

        assertEquals(expected, sql)
    }
}
