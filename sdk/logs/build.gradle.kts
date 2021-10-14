plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Contrib Logging Support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.logging")

dependencies {
  api(project(":sdk:all"))

  implementation(project(":api:metrics"))

  implementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
