package integration

import java.sql.Connection
import java.sql.DriverManager

class TestDatabase(jdbcUrl: String, user: String, password: String) {

    private val connection: Connection = DriverManager.getConnection(jdbcUrl, user, password)

    fun executeBatch(sql: String) {
        sql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { connection.createStatement().execute("$it;") }
    }

    fun query(sql: String) =
        connection.createStatement().executeQuery(sql)

    fun queryColumn(sql: String, column: String): List<String> {
        val rs = query(sql)
        val result = mutableListOf<String>()

        while (rs.next()) {
            result.add(rs.getString(column))
        }

        return result
    }
}
