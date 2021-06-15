plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Semantic Conventions"
extra["moduleName"] = "io.opentelemetry.semconv"

dependencies {
    api(project(":api:all"))
}
