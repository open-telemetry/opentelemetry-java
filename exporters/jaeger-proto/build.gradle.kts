plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Jaeger Exporter Proto (Internal Use Only)"
otelJava.moduleName.set("io.opentelemetry.exporter.jaeger.proto")

dependencies {
  api("com.google.protobuf:protobuf-java")

  compileOnly("io.grpc:grpc-api")
  compileOnly("io.grpc:grpc-protobuf")
  compileOnly("io.grpc:grpc-stub")
}
