plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("otel.jmh-conventions")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API Incubator"
otelJava.moduleName.set("io.opentelemetry.extension.incubator")

dependencies {
    api(project(":api:all"))

    testImplementation(project(":sdk:testing"))
}
