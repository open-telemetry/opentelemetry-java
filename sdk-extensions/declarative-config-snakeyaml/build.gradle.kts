plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Declarative Config SnakeYAML Parser"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.declarativeconfig.snakeyaml")
otelJava.requireSuppressWarningsExplanation.set(false)
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationYamlParser",
))

dependencies {
  api(project(":sdk-extensions:autoconfigure-spi"))
  implementation("org.snakeyaml:snakeyaml-engine")
}
