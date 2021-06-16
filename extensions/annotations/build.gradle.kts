plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Extension Annotations"
extra["moduleName"] = "io.opentelemetry.extension.annotations"

dependencies {
    api(project(":api:all"))
}
