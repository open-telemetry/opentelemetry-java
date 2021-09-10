plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Logging Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.logging")

dependencies {
  api(project(":sdk:opentelemetry-sdk"))
  api(project(":sdk:opentelemetry-sdk-metrics"))

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
}
