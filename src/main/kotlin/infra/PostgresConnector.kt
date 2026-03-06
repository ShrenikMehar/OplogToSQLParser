package infra

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

fun connectPostgres(config: Properties): Connection? {

    println("Connecting to Postgres...")

    val url = config.getProperty("postgres.url")
    val user = config.getProperty("postgres.user")
    val password = config.getProperty("postgres.password")

    repeat(10) { attempt ->

        try {

            val connection = DriverManager.getConnection(url, user, password)

            println("Connected to Postgres!")

            return connection

        } catch (_: Exception) {

            println("Attempt ${attempt + 1}: Postgres not ready yet...")
            Thread.sleep(2000)

        }

    }

    println("Failed to connect to Postgres.")

    return null
}
