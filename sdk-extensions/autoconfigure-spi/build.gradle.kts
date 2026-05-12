plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Auto-configuration SPI"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.spi")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator"))

dependencies {
  api(project(":sdk:all"))
  compileOnly(project(":api:incubator"))
}
