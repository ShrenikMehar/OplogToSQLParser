plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("com.gradleup.shadow") version "9.2.2"
}

group = "one2n.parser"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.6")
    implementation("org.apache.kafka:kafka-clients:4.2.0")
    implementation("org.postgresql:postgresql:42.7.10")

    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("app.jar")

    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}