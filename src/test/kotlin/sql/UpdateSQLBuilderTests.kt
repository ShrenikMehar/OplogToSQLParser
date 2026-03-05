package sql

import json.OplogJsonAccessor
import json.OplogJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateSQLBuilderTests {

    private val parser = OplogJsonParser()
    private val accessor = OplogJsonAccessor()
    private val sqlUtils = SqlUtils()

    private val builder = UpdateSQLBuilder(accessor, sqlUtils)

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

    @Test
    fun `should generate update sql when diff u is present`() {
        val node = parser.parse(updateJsonForNewValue())

        val sql = builder.build(node)

        assertEquals(
            "UPDATE test.student SET is_graduated = true WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should generate update sql when diff d is present`() {
        val node = parser.parse(updateJsonForUnsetting())

        val sql = builder.build(node)

        assertEquals(
            "UPDATE test.student SET roll_no = NULL WHERE _id = '635b79e231d82a8ab1de863b';",
            sql
        )
    }

    @Test
    fun `should throw exception for unsupported update operation`() {
        val json = """
        {
           "op": "u",
           "ns": "test.student",
           "o": {
              "diff": {}
           },
           "o2": {
              "_id": "635b79e231d82a8ab1de863b"
           }
        }
        """.trimIndent()

        val node = parser.parse(json)

        assertFailsWith<IllegalArgumentException> {
            builder.build(node)
        }
    }
}
