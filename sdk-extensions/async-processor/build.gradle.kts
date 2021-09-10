plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Extension: Async SpanProcessor"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.export")

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk"))

  implementation("com.google.guava:guava")
  implementation("com.lmax:disruptor")
}
