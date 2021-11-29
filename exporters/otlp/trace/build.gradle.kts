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

  // Mainly to conveniently profile through IDEA. Don't add to check task, it's for
  // manual invocation.
  create("testSpanPipeline")
}

dependencies {
  api(project(":sdk:trace"))
  api(project(":exporters:otlp:common"))

  implementation(project(":api:metrics"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))
  testImplementation(project(":sdk:testing"))

  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-stub")

  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")
  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-stub")

  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")
  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-stub")

  add("testSpanPipelineImplementation", "io.grpc:grpc-protobuf")
  add("testSpanPipelineImplementation", "io.grpc:grpc-testing")
  add("testSpanPipelineImplementation", "io.opentelemetry.proto:opentelemetry-proto")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation("com.linecorp.armeria:armeria")
  jmhImplementation("com.linecorp.armeria:armeria-grpc")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("com.squareup.okhttp3:okhttp")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

tasks {
  check {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp")
  }
}
