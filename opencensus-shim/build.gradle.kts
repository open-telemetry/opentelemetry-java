plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry OpenCensus Shim"
otelJava.moduleName.set("io.opentelemetry.opencensusshim")

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":extensions:opentelemetry-extension-trace-propagators"))
  api(project(":sdk:opentelemetry-sdk"))
  api(project(":sdk:opentelemetry-sdk-metrics"))

  api("io.opencensus:opencensus-api")
  api("io.opencensus:opencensus-impl-core")
  api("io.opencensus:opencensus-exporter-metrics-util")

  testImplementation(project(":sdk:opentelemetry-sdk"))
  testImplementation(project(":sdk:opentelemetry-sdk-metrics-testing"))

  testImplementation("org.slf4j:slf4j-simple")
  testImplementation("io.opencensus:opencensus-impl")
}
