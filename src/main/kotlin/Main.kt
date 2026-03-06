import java.sql.DriverManager

fun main() {
    println("Connecting to Postgres...")

    val url = "jdbc:postgresql://postgres:5432/oplogdb"
    val user = "postgres"
    val password = "postgres"

    repeat(10) { attempt ->
        try {
            val conn = DriverManager.getConnection(url, user, password)
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT 1")

            while (rs.next()) {
                println("DB result: ${rs.getInt(1)}")
            }

            conn.close()
            return
        } catch (e: Exception) {
            println("Attempt ${attempt + 1}: Postgres not ready yet...")
            Thread.sleep(2000)
        }
    }

    println("Failed to connect to Postgres.")
}
