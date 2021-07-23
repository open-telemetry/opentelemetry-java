plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")

    id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Logging Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.logging")

dependencies {
    api(project(":sdk:logging"))

    implementation(project(":exporters:otlp:common"))

    implementation("io.grpc:grpc-api")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java")

    testImplementation(project(":sdk:testing"))

    testImplementation("io.grpc:grpc-testing")
    testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
