plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Semantic Conventions"
otelJava.moduleName.set("io.opentelemetry.semconv")

dependencies {
  api(project(":api:all"))
}
