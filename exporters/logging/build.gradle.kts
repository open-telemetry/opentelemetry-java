plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Logging Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.logging")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator", "io.opentelemetry.sdk.autoconfigure.spi"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
))

dependencies {
  api(project(":sdk:all"))

  compileOnly(project(":sdk-extensions:autoconfigure-spi"))
  compileOnly(project(":api:incubator"))

  testImplementation(project(":sdk:testing"))
}
