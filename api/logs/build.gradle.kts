plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Logs API"
otelJava.moduleName.set("io.opentelemetry.api.logs")

dependencies {
  api(project(":api:all"))
}
