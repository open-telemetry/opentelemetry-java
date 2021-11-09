plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
  id("otel.jmh-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry Protocol Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.trace")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
  create("testOkhttpOnly")

  // Mainly to conveniently profile through IDEA. Don't add to check task, it's for
  // manual invocation.
  create("testSpanPipeline")
}

dependencies {
  api(project(":sdk:trace"))
  api(project(":exporters:otlp:common"))

  implementation(project(":api:metrics"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testImplementation("org.slf4j:slf4j-simple")

  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNettyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")
  add("testGrpcNettyShadedRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")
  add("testGrpcOkhttpRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testOkhttpOnlyImplementation", "com.linecorp.armeria:armeria-grpc-protocol")
  add("testOkhttpOnlyImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testOkhttpOnlyImplementation", "com.squareup.okhttp3:okhttp-tls")
  add("testOkhttpOnlyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testSpanPipeline", "io.grpc:grpc-protobuf")
  add("testSpanPipeline", "io.grpc:grpc-testing")
  add("testSpanPipeline", "io.opentelemetry.proto:opentelemetry-proto")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation("com.linecorp.armeria:armeria")
  jmhImplementation("com.linecorp.armeria:armeria-grpc")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("com.squareup.okhttp3:okhttp")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

tasks {
  check {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp", "testOkhttpOnly")
  }
}

configurations {
  named("testOkhttpOnlyRuntimeClasspath") {
    dependencies {
      exclude("io.grpc")
    }
  }
}
