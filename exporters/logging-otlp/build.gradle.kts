plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Protocol JSON Logging Exporters"
extra["moduleName"] = "io.opentelemetry.exporter.logging.otlp"

dependencies {
    compileOnly(project(":sdk:trace"))
    compileOnly(project(":sdk:metrics"))

    implementation(project(":sdk-extensions:otproto"))

    implementation("org.curioswitch.curiostack:protobuf-jackson")

    testImplementation(project(":sdk:testing"))

    testImplementation("org.skyscreamer:jsonassert")
}
