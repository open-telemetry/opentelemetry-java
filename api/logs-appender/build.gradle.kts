plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Log Appender API"
otelJava.moduleName.set("io.opentelemetry.api.logs.appender")

dependencies {
  api(project(":api:all"))
}
