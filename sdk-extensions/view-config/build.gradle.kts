plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry View Config SDK Extension"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.view.config")

dependencies {
  implementation(project(":sdk:metrics"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  implementation("com.fasterxml.jackson.core:jackson-core")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

  annotationProcessor("com.google.auto.value:auto-value")
}
