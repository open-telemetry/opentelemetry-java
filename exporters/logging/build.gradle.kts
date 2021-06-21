plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Logging Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.logging")

dependencies {
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    testImplementation(project(":sdk:testing"))
}
