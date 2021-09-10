plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Tracing Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.incubator")

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk"))

  implementation(project(":api:opentelemetry-api-metrics"))
  implementation(project(":opentelemetry-semconv"))
  implementation("org.jctools:jctools-core:3.3.0")

  annotationProcessor("com.google.auto.value:auto-value")
  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation("com.google.guava:guava-testlib")

  jmh(project(":sdk:opentelemetry-sdk-metrics"))
  jmh(project(":sdk:opentelemetry-sdk-testing")) {
    // JMH doesn"t handle dependencies that are duplicated between the main and jmh
    // configurations properly, but luckily here it"s simple enough to just exclude transitive
    // dependencies.
    isTransitive = false
  }
  jmh(project(":exporters:otlp:opentelemetry-exporter-otlp-trace")) {
    // The opentelemetry-exporter-otlp-trace depends on this project itself. So don"t pull in
    // the transitive dependencies.
    isTransitive = false
  }
  // explicitly adding the opentelemetry-exporter-otlp dependencies
  jmh(project(":exporters:otlp:opentelemetry-exporter-otlp-common")) {
    isTransitive = false
  }
  jmh(project(":opentelemetry-proto"))

  jmh("com.google.guava:guava")
  jmh("io.grpc:grpc-api")
  jmh("io.grpc:grpc-netty-shaded")
  jmh("org.testcontainers:testcontainers") // testContainer for OTLP collector
}
