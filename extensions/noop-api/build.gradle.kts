plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Noop API"

otelJava.moduleName.set("io.opentelemetry.extension.noopapi")

dependencies { api(project(":api:all")) }
