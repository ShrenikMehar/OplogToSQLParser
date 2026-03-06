package infra

import java.util.Properties

fun loadConfig(): Properties {

    val props = Properties()

    val stream = object {}.javaClass
        .getResourceAsStream("/application.properties")
        ?: throw RuntimeException("application.properties not found")

    props.load(stream)

    return props
}
