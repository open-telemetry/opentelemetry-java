plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Protocol Metrics Exporter"
extra["moduleName"] = "io.opentelemetry.exporter.otlp.metrics"

dependencies {
    api(project(":sdk:metrics"))

    implementation(project(":sdk-extensions:otproto"))

    implementation("io.grpc:grpc-api")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java")

    testImplementation(project(":sdk:testing"))

    testImplementation("io.grpc:grpc-testing")
    testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
