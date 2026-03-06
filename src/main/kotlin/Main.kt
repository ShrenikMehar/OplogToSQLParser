import parser.OplogToSQLParser
import java.sql.DriverManager

fun main() {
    println("Connecting to Postgres...")

    val url = "jdbc:postgresql://postgres:5432/oplogdb"
    val user = "postgres"
    val password = "postgres"

    var connection: java.sql.Connection? = null

    repeat(10) { attempt ->
        try {
            connection = DriverManager.getConnection(url, user, password)
            println("Connected to Postgres!")
            return@repeat
        } catch (e: Exception) {
            println("Attempt ${attempt + 1}: Postgres not ready yet...")
            Thread.sleep(2000)
        }
    }

    if (connection == null) {
        println("Failed to connect to Postgres.")
        return
    }

    val parser = OplogToSQLParser()

    val json = object {}.javaClass
        .getResource("/sample-oplog.json")!!
        .readText()

    val sql = parser.toSQL(json)

    println("Generated SQL:")
    println(sql)

    connection!!.createStatement().execute(sql)

    println("SQL executed successfully")

    connection!!.close()
}
