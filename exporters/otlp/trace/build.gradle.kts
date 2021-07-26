plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")

    id("otel.jmh-conventions")
    id("org.unbroken-dome.test-sets")
    id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.trace")

testSets {
    create("testGrpcNetty")
    create("testGrpcNettyShaded")
    create("testGrpcOkhttp")
}

dependencies {
    api(project(":sdk:trace"))

    compileOnly("io.grpc:grpc-netty")
    compileOnly("io.grpc:grpc-netty-shaded")
    compileOnly("io.grpc:grpc-okhttp")

    implementation(project(":exporters:otlp:common"))
    implementation("io.grpc:grpc-api")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    implementation("com.google.protobuf:protobuf-java")

    testImplementation(project(":sdk:testing"))

    testImplementation("io.grpc:grpc-testing")
    testImplementation("org.slf4j:slf4j-simple")

    add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-grpc")
    add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-junit5")
    add("testGrpcNettyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

    add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-grpc")
    add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-junit5")
    add("testGrpcNettyShadedRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

    add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-grpc")
    add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-junit5")
    add("testGrpcOkhttpRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

    add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")

    add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")

    add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")

    jmh(project(":sdk:testing"))
}

tasks {
    named("check") {
        dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp")
    }
}
