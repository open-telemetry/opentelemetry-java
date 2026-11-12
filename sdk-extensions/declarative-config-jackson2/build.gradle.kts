plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Declarative Config Jackson 2 Mapper"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.declarativeconfig.jackson2")
otelJava.requireSuppressWarningsExplanation.set(false)
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationMapper",
))

dependencies {
  api(project(":sdk-extensions:autoconfigure-spi"))
  implementation(project(":api:incubator"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
}
