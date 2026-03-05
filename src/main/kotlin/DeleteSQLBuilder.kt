import com.fasterxml.jackson.databind.JsonNode

class DeleteSQLBuilder(
    private val accessor: OplogJsonAccessor
) {

    fun build(node: JsonNode): String {
        val table = accessor.getNamespace(node)
        val id = accessor.getObjectNode(node).get("_id").asText()

        return "DELETE FROM $table WHERE _id = '$id';"
    }
}
