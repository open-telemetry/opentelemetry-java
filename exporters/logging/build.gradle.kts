plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Logging Exporter"
extra["moduleName"] = "io.opentelemetry.exporter.logging"

dependencies {
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    testImplementation(project(":sdk:testing"))
}
