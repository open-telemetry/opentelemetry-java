plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry Protocol Logs Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.logs")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
}

dependencies {
  api(project(":sdk:logs"))
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
  named("check") {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp")
  }
}
