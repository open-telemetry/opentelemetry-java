plugins {
    java
}

description = "OpenTelemetry Integration Tests"
extra["moduleName"] = "io.opentelemetry.integration.tests"

dependencies {
    implementation(project(":sdk:all"))
    implementation(project(":exporters:jaeger"))
    implementation(project(":semconv"))

    implementation("io.grpc:grpc-protobuf")
    implementation("com.google.protobuf:protobuf-java")
    implementation("io.grpc:grpc-netty-shaded")

    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.squareup.okhttp3:okhttp")
}
