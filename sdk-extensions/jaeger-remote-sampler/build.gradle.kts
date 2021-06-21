plugins {
    id("otel.protobuf-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Jaeger Remote sampler"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.jaeger")

dependencies {
    api(project(":sdk:all"))

    implementation(project(":sdk:all"))
    implementation("io.grpc:grpc-api")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java")

    testImplementation("io.grpc:grpc-testing")
    testImplementation("org.testcontainers:junit-jupiter")

    testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
