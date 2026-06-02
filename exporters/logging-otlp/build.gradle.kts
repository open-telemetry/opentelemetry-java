plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol JSON Logging Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.logging.otlp")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator", "io.opentelemetry.sdk.autoconfigure.spi"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
))

dependencies {
  implementation(project(":sdk:trace"))
  implementation(project(":sdk:metrics"))
  implementation(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))
  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:autoconfigure-spi"))

  implementation("com.fasterxml.jackson.core:jackson-core")

  testImplementation(project(":api:incubator"))
  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation("com.google.guava:guava")
  testImplementation("org.skyscreamer:jsonassert")
}
