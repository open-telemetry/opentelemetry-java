plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API Extensions for AWS"
otelJava.moduleName.set("io.opentelemetry.extension.aws")

dependencies {
    api(project(":api:all"))
    compileOnly(project(":sdk-extensions:autoconfigure"))
}
