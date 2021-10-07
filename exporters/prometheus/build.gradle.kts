plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Prometheus Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.prometheus")

dependencies {
  api(project(":sdk:metrics"))

  api("io.prometheus:simpleclient")
  implementation("io.prometheus:simpleclient_common")

  compileOnly("com.sun.net.httpserver:http")

  testImplementation("com.google.guava:guava")
  testImplementation("com.linecorp.armeria:armeria")
}
