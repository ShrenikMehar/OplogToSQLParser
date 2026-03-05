package integration

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.DriverManager
import kotlin.test.assertEquals

@Testcontainers
class PostgresConnectionTest: BasePostgresTest() {

    @Test
    fun `should connect to postgres`() {

        val connection = DriverManager.getConnection(
            postgres.jdbcUrl,
            postgres.username,
            postgres.password
        )

        val result = connection
            .createStatement()
            .executeQuery("SELECT 1")
            .apply { next() }
            .getInt(1)

        assertEquals(1, result)
    }
}
