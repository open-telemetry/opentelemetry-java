plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Declarative Config Jackson Mapper"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.declarativeconfig.jackson")
otelJava.requireSuppressWarningsExplanation.set(false)
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationMapper",
))

dependencies {
  api(project(":sdk-extensions:declarative-config-spi"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
}
