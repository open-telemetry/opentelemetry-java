plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Prometheus Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.prometheus")

dependencies {
  api(project(":sdk:opentelemetry-sdk-metrics"))

  api("io.prometheus:simpleclient")

  testImplementation("io.prometheus:simpleclient_common")
  testImplementation("com.google.guava:guava")
}
