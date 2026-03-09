import infra.loadConfig
import infra.connectPostgres
import infra.createKafkaConsumer
import infra.DebeziumAdapter
import parser.OplogToSQLParser
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.time.Duration

fun main() {

    val config = loadConfig()

    val connection = connectPostgres(config) ?: return

    val consumer = createKafkaConsumer(config)

    val parser = OplogToSQLParser()
    val adapter = DebeziumAdapter()

    println("Kafka consumer started. Waiting for messages...")

    while (true) {

        val records: ConsumerRecords<String, String> =
            consumer.poll(Duration.ofMillis(100))

        for (record in records) {

            try {

                val rawEvent = record.value()

                println("Received message from Kafka:")
                println(rawEvent)

                val normalizedEvent = adapter.normalize(rawEvent)

                println("Normalized oplog event:")
                println(normalizedEvent)

                val sql = parser.toSQL(normalizedEvent)

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
