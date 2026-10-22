plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Auto-configuration SPI"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.spi")
otelJava.requireSuppressWarningsExplanation.set(false)
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator"))

dependencies {
  api(project(":sdk:all"))
  compileOnly(project(":api:incubator"))
}

tasks.test {
  // Configure environment variable for ConfigPropertiesTest
  environment("OTEL_JAVA_EXPORTER_MEMORY_MODE", "immutable_data")
}
