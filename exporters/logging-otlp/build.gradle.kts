plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol JSON Logging Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.logging.otlp")

dependencies {
  compileOnly(project(":sdk:opentelemetry-sdk-trace"))
  compileOnly(project(":sdk:opentelemetry-sdk-metrics"))

  implementation(project(":exporters:otlp:opentelemetry-exporter-otlp-common"))
  implementation(project(":opentelemetry-proto"))

  implementation("org.curioswitch.curiostack:protobuf-jackson")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))

  testImplementation("org.skyscreamer:jsonassert")
}
