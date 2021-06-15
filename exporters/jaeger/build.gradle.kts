plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("com.google.protobuf")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Jaeger Exporter"
extra["moduleName"] = "io.opentelemetry.exporter.jaeger"

dependencies {
    api(project(":sdk:all"))
    api("io.grpc:grpc-api")

    implementation(project(":sdk:all"))
    implementation(project(":semconv"))

    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java")
    implementation("com.google.protobuf:protobuf-java-util")

    testImplementation("io.grpc:grpc-testing")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.squareup.okhttp3:okhttp")

    testImplementation(project(":sdk:testing"))

    testRuntimeOnly("io.grpc:grpc-netty-shaded")
}

// IntelliJ complains that the generated classes are not found, ask IntelliJ to include the
// generated Java directories as source folders.
idea {
    module {
        sourceDirs.add(file("build/generated/source/proto/main/java"))
        // If you have additional sourceSets and/or codegen plugins, add all of them
    }
}
