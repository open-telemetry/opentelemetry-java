plugins {
  id("otel.java-conventions")
  // TODO (jack-berg): uncomment when ready to publish
  // id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profiles Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.profiles")

dependencies {
  api(project(":sdk:common"))
}
