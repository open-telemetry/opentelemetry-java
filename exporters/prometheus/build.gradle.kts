plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Prometheus Exporter"
extra["moduleName"] = "io.opentelemetry.exporter.prometheus"

dependencies {
    api(project(":sdk:metrics"))

    api("io.prometheus:simpleclient")

    testImplementation("io.prometheus:simpleclient_common")
    testImplementation("com.google.guava:guava")
}
