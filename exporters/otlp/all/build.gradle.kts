plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("me.champeau.jmh")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Protocol Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp")
base.archivesBaseName = "opentelemetry-exporter-otlp"

dependencies {
    api(project(":exporters:otlp:trace"))
}
