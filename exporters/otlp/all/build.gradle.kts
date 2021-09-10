plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp")

dependencies {
  api(project(":exporters:otlp:opentelemetry-exporter-otlp-trace"))
}
