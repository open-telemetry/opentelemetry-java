plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Incubator"
otelJava.moduleName.set("io.opentelemetry.extension.incubator")

dependencies {
  api(project(":api:opentelemetry-api"))

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
}
