plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Protocol Exporter"
extra["moduleName"] = "io.opentelemetry.exporters.otprotocol"

dependencies {
    api(project(":api:all"))
    api(project(":proto"))
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    implementation("com.google.protobuf:protobuf-java")

    testImplementation(project(":sdk:testing"))

    testImplementation("io.grpc:grpc-testing")
    testRuntime("io.grpc:grpc-netty-shaded")
}
