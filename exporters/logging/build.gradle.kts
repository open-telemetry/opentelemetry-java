plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Logging Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.logging")
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
))

dependencies {
  api(project(":sdk:all"))

  implementation(project(":sdk-extensions:autoconfigure-spi"))
  compileOnly(project(":api:incubator"))

  testImplementation(project(":sdk:testing"))
}
