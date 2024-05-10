plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profiles Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.profiles")

dependencies {
  api(project(":sdk:common"))
}
