plugins {
    `java-library`
    `maven-publish`

    id("me.champeau.jmh")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API Incubator"
extra["moduleName"] = "io.opentelemetry.extension.incubator"

dependencies {
    api(project(":api:all"))

    testImplementation(project(":sdk:testing"))
}
