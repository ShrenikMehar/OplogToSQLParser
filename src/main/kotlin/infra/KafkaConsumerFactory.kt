package infra

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.Properties

fun createKafkaConsumer(config: Properties): KafkaConsumer<String, String> {

    println("Connecting to Kafka...")

    val kafkaProps = Properties().apply {

        put("bootstrap.servers", config.getProperty("kafka.bootstrapServers"))
        put("group.id", config.getProperty("kafka.groupId"))

        put(
            "key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer"
        )

        put(
            "value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer"
        )

        put("auto.offset.reset", "earliest")

    }

    val consumer = KafkaConsumer<String, String>(kafkaProps)

    consumer.subscribe(listOf(config.getProperty("kafka.topic")))

    return consumer
}
