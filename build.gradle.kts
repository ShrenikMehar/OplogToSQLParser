plugins {
    kotlin("jvm") version "2.3.0"
    application
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
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.jar {
    archiveFileName.set("app.jar")

    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}