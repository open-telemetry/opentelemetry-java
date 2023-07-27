import io.opentelemetry.gradle.OtelVersionClassPlugin

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}
apply<OtelVersionClassPlugin>()

description = "OpenTelemetry OpenCensus Shim"
otelJava.moduleName.set("io.opentelemetry.opencensusshim")

dependencies {
  api(project(":api:all"))
  api(project(":extensions:trace-propagators"))
  api(project(":sdk:all"))

  api("io.opencensus:opencensus-api")
  api("io.opencensus:opencensus-impl-core")
  api("io.opencensus:opencensus-exporter-metrics-util")

  testImplementation(project(":sdk:all"))
  testImplementation(project(":sdk:testing"))

  testImplementation("io.opencensus:opencensus-impl")
  testImplementation("io.opencensus:opencensus-contrib-exemplar-util")
}

tasks.named<Test>("test") {
  // We must force a fork per-test class because OpenCensus pollutes globals with no restorative
  // methods available.
  setForkEvery(1)
  maxParallelForks = 3
}
