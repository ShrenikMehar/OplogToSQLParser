import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.DriverManager
import kotlin.test.assertEquals

@Testcontainers
class PostgresConnectionTest {

    companion object {

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }

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
