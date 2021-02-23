plugins {
    `java-library`
    `maven-publish`

    id("com.google.protobuf")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Jaeger Remote sampler"
extra["moduleName"] = "io.opentelemetry.sdk.extension.trace.jaeger"

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

// IntelliJ complains that the generated classes are not found, ask IntelliJ to include the
// generated Java directories as source folders.
idea {
    module {
        sourceDirs.add(file("build/generated/source/proto/main/java"))
        // If you have additional sourceSets and/or codegen plugins, add all of them
    }
}
