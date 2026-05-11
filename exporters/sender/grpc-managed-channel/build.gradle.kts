plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry gRPC Upstream Sender"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.grpc.managedchannel.internal")

dependencies {
  annotationProcessor("com.google.auto.value:auto-value")

  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("io.grpc:grpc-stub")
}
