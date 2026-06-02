plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Zipkin Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.zipkin")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.sdk.autoconfigure.spi"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider",
))

dependencies {
  api(project(":sdk:all"))

  api("io.zipkin.reporter2:zipkin-reporter")

  implementation(project(":exporters:common"))
  compileOnly(project(":sdk-extensions:autoconfigure-spi"))
  compileOnly(project(":api:incubator"))

  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation("com.linecorp.armeria:armeria")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}
