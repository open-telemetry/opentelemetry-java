plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")

    id("otel.jmh-conventions")
    id("org.unbroken-dome.test-sets")
    id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Context (Incubator)"
otelJava.moduleName.set("io.opentelemetry.context")

testSets {
    create("grpcInOtelTest")
    create("otelInGrpcTest")

    create("braveInOtelTest")
    create("otelInBraveTest")
    create("otelAsBraveTest")

    create("storageWrappersTest")

    create("strictContextEnabledTest")
}

dependencies {
    add("grpcInOtelTestImplementation", "io.grpc:grpc-context")
    add("otelInGrpcTestImplementation", "io.grpc:grpc-context")

    add("braveInOtelTestImplementation", "io.zipkin.brave:brave")
    add("otelAsBraveTestImplementation", "io.zipkin.brave:brave")
    add("otelInBraveTestImplementation", "io.zipkin.brave:brave")

    add("strictContextEnabledTestImplementation", project(":api:all"))

    // MustBeClosed
    compileOnly("com.google.errorprone:error_prone_annotations")

    testImplementation("org.awaitility:awaitility")
    testImplementation("com.google.guava:guava")
    testImplementation("org.junit-pioneer:junit-pioneer")
}

tasks {
    named<Test>("strictContextEnabledTest") {
        jvmArgs("-Dio.opentelemetry.context.enableStrictContext=true")
    }

    named("check") {
        dependsOn("grpcInOtelTest", "otelInGrpcTest", "braveInOtelTest", "otelInBraveTest",
                "otelAsBraveTest", "storageWrappersTest", "strictContextEnabledTest")
    }
}

