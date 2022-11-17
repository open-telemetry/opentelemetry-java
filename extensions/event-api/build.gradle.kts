plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Event API"
otelJava.moduleName.set("io.opentelemetry.extension.events")

dependencies {
  api(project(":api:logs"))
  implementation(project(":semconv"))
}
