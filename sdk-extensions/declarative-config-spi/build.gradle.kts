plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Declarative Config SPI"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi")
otelJava.requireSuppressWarningsExplanation.set(false)

dependencies {
  api(project(":sdk-extensions:autoconfigure-spi"))
}
