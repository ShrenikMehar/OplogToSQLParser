package sql

import json.OplogJsonAccessor
import json.OplogJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteSQLBuilderTests {

    private val parser = OplogJsonParser()
    private val accessor = OplogJsonAccessor()

    private val builder = DeleteSQLBuilder(accessor)

    private fun deleteJson(): String {
        return javaClass
            .getResource("/oplog-delete.json")!!
            .readText()
    }

    @Test
    fun `should generate delete sql`() {
        val node = parser.parse(deleteJson())

        val sql = builder.build(node)

        assertEquals(
            "DELETE FROM test.student WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }
}
