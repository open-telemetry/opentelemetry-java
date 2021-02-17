plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API Extensions for AWS"
extra["moduleName"] = "io.opentelemetry.extension.aws"

dependencies {
    api(project(":api:all"))
    compileOnly(project(":sdk-extensions:autoconfigure"))
}
