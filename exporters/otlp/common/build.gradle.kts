plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.internal")

dependencies {
  protoSource(project(":proto"))

  api(project(":api:all"))
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))

  // We only use the protos for HTTP, logging-otlp, and metric export for now. Let them expose the
  // proto dependency in their POMs instead of here. This is fine because this artifact only
  // contains internal code.
  compileOnly(project(":proto"))

  // Similar to above note about :proto, we include a helper shared by the gRPC exporters but do not
  // want to impose the gRPC dependency on all of our consumers.
  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":proto"))
  testImplementation(project(":sdk:testing"))

  testImplementation("org.jeasy:easy-random-randomizers")

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation(project(":sdk-extensions:resources"))
}

wire {
  root(
    "opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest",
    "opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest",
  )

  custom {
    customHandlerClass = "io.opentelemetry.gradle.ProtoFieldsWireHandler"
  }
}
