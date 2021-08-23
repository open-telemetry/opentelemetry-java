plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Logging Exporter"

otelJava.moduleName.set("io.opentelemetry.exporter.logging")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))

  testImplementation(project(":sdk:testing"))
}
