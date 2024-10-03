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
  compileOnly(project(":api:incubator"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  implementation("com.fasterxml.jackson.core:jackson-core")

  testImplementation(project(":api:incubator"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.guava:guava")
  testImplementation("org.skyscreamer:jsonassert")
}
