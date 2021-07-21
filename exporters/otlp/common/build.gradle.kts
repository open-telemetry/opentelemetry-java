plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")

    id("otel.jmh-conventions")
    id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.internal")

dependencies {
    api(project(":api:all"))
    api(project(":proto"))
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    api("org.curioswitch.curiostack:protobuf-jackson")
    implementation("com.google.protobuf:protobuf-java")

    testImplementation(project(":sdk:testing"))

    testImplementation("io.grpc:grpc-testing")
    testRuntimeOnly("io.grpc:grpc-netty-shaded")

    jmhImplementation(project(":sdk:testing"))
    jmhImplementation(project(":sdk-extensions:resources"))
}
