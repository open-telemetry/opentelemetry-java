plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("org.unbroken-dome.test-sets")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Metrics Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.metrics")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
}

dependencies {
  api(project(":sdk:metrics"))
  api(project(":exporters:otlp:common"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))
  testImplementation(project(":sdk:testing"))

  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-stub")

  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")
  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-stub")

  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")
  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-stub")
}

tasks {
  check {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp")
  }
}
