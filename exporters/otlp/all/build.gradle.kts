plugins {
    `java-library`
    `maven-publish`

    id("me.champeau.jmh")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Protocol Exporters"
extra["moduleName"] = "io.opentelemetry.exporter.otlp"
base.archivesBaseName = "opentelemetry-exporter-otlp"

dependencies {
    api(project(":exporters:otlp:trace"))
}
