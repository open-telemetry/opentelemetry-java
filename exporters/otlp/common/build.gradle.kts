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
  protoSource(project(":opentelemetry-proto"))

  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk"))
  api(project(":sdk:opentelemetry-sdk-metrics"))

  // We only use the protos for HTTP, logging-otlp, and metric export for now. Let them expose the
  // proto dependency in their POMs instead of here. This is fine because this artifact only
  // contains internal code.
  compileOnly(project(":opentelemetry-proto"))

  compileOnly("com.fasterxml.jackson.core:jackson-core")

  // Similar to above note about :proto, we include helpers shared by gRPC or okhttp exporters but
  // do not want to impose these dependency on all of our consumers.
  compileOnly("com.squareup.okhttp3:okhttp")
  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")
  compileOnly("io.grpc:grpc-stub")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":opentelemetry-proto"))
  testImplementation(project(":sdk:opentelemetry-sdk-testing"))

  testImplementation("com.fasterxml.jackson.core:jackson-core")
  testImplementation("com.google.protobuf:protobuf-java-util")

  testImplementation("org.jeasy:easy-random-randomizers")

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

  jmhImplementation(project(":opentelemetry-proto"))
  jmhImplementation(project(":sdk:opentelemetry-sdk-testing"))
  jmhImplementation(project(":sdk-extensions:opentelemetry-sdk-extension-resources"))
  jmhImplementation("com.fasterxml.jackson.core:jackson-core")
  jmhImplementation("org.curioswitch.curiostack:protobuf-jackson")
  jmhImplementation("com.google.protobuf:protobuf-java-util")
  jmhRuntimeOnly("io.grpc:grpc-netty")
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
