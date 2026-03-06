import parser.OplogToSQLParser
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.sql.DriverManager
import java.time.Duration
import java.util.Properties

fun main() {

    val config = Properties()
    val configStream = object {}.javaClass.getResourceAsStream("/application.properties")
    config.load(configStream)

    println("Connecting to Postgres...")

    val url = config.getProperty("postgres.url")
    val user = config.getProperty("postgres.user")
    val password = config.getProperty("postgres.password")

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

    println("Connecting to Kafka...")

    val kafkaProps = Properties().apply {
        put("bootstrap.servers", config.getProperty("kafka.bootstrapServers"))
        put("group.id", config.getProperty("kafka.groupId"))
        put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        put("auto.offset.reset", "earliest")
    }

    val consumer = KafkaConsumer<String, String>(kafkaProps)
    consumer.subscribe(listOf(config.getProperty("kafka.topic")))

    println("Kafka consumer started. Waiting for messages...")

    val parser = OplogToSQLParser()

    while (true) {
        val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofMillis(100))

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
