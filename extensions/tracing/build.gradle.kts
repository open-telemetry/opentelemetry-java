plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API Extensions for Tracing"
extra["moduleName"] = "io.opentelemetry.extension.tracing"

dependencies {
    api(project(":api:all"))

    testImplementation(project(":sdk:testing"))
}
