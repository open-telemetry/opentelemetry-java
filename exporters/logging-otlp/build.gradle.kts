plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol JSON Logging Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.logging.otlp")

dependencies {
  implementation(project(":sdk:trace"))
  implementation(project(":sdk:metrics"))
  implementation(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  implementation("com.fasterxml.jackson.core:jackson-core")

  testImplementation(project(":sdk:testing"))

  testImplementation("org.skyscreamer:jsonassert")
}
