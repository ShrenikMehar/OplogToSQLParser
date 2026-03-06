import infra.loadConfig
import infra.connectPostgres
import infra.createKafkaConsumer
import parser.OplogToSQLParser
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.time.Duration

fun main() {

    val config = loadConfig()

    val connection = connectPostgres(config) ?: return

    val consumer = createKafkaConsumer(config)

    val parser = OplogToSQLParser()

    println("Kafka consumer started. Waiting for messages...")

    while (true) {

        val records: ConsumerRecords<String, String> =
            consumer.poll(Duration.ofMillis(100))

        for (record in records) {

            try {

                val json = record.value()

                println("Received message from Kafka:")
                println(json)

                val sql = parser.toSQL(json)

                println("Generated SQL:")
                println(sql)

                connection.createStatement().execute(sql)

                println("SQL executed successfully")

            } catch (e: Exception) {

                println("Error processing message: ${e.message}")

            }

        }

    }
}
