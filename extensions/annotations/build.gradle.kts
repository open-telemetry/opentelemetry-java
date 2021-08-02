plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Extension Annotations"
otelJava.moduleName.set("io.opentelemetry.extension.annotations")

dependencies {
  api(project(":api:all"))
}
