plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Jaeger Thrift Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.jaeger.thrift")

dependencies {
  api(project(":sdk:opentelemetry-sdk"))

  implementation(project(":sdk:opentelemetry-sdk"))
  implementation(project(":opentelemetry-semconv"))

  implementation("io.jaegertracing:jaeger-client")

  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("com.google.guava:guava-testlib")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
}
