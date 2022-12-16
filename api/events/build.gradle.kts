plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Events API"
otelJava.moduleName.set("io.opentelemetry.api.events")

dependencies {
  api(project(":api:all"))
}
