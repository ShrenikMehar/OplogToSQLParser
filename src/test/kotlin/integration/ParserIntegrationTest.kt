package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import parser.OplogToSQLParser

class ParserIntegrationTest : BasePostgresTest() {

    private fun inputJson(): String =
        javaClass.getResource("/oplog-insert-multiple.json")!!.readText()

    @Test
    fun `should execute parser SQL and insert row into postgres`() {

        val parser = OplogToSQLParser()
        val db = TestDatabase(postgres.jdbcUrl, postgres.username, postgres.password)

        db.executeBatch(parser.toSQL(inputJson()))

        val students = db.queryColumn(
            "SELECT name FROM test.student ORDER BY name",
            "name"
        )

        assertEquals(
            listOf("George Smith", "Selena Miller"),
            students
        )
    }
}
