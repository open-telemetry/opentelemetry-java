plugins {
  id("otel.java-conventions")
}

description = "Performance Testing Harness"
otelJava.moduleName.set("io.opentelemetry.perf-harness")

dependencies {
  implementation(project(":api:opentelemetry-api"))
  implementation(project(":sdk:opentelemetry-sdk"))
  implementation(project(":sdk:opentelemetry-sdk-testing"))
  implementation(project(":exporters:otlp:opentelemetry-exporter-otlp-trace"))
  implementation(project(":exporters:opentelemetry-exporter-logging"))
  implementation(project(":opentelemetry-semconv"))

  implementation("eu.rekawek.toxiproxy:toxiproxy-java")
  implementation("org.testcontainers:junit-jupiter")

  runtimeOnly("io.grpc:grpc-netty-shaded")
}
