plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry SDK Extension: Async SpanProcessor"
extra["moduleName"] = "io.opentelemetry.sdk.extension.trace.export"

dependencies {
    api(project(":api:all"))
    api(project(":sdk:all"))

    implementation("com.google.guava:guava")
    implementation("com.lmax:disruptor")
}
