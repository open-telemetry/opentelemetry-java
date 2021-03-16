plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry SDK Extension: ExecutorService SpanProcessor"
extra["moduleName"] = "io.opentelemetry.sdk.extension.trace.export"

dependencies {
    api(project(":api:all"))
    api(project(":api:metrics"))
    api(project(":sdk:all"))
}
