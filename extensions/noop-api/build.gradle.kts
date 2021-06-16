plugins {
    id("otel.java-conventions")
    id("maven-publish")

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Noop API"
otelJava.moduleName.set("io.opentelemetry.extension.noopapi")

dependencies {
    api(project(":api:all"))
}
