plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Contrib Logging Support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.logging")

dependencies {
  api(project(":sdk:common"))

  implementation(project(":api:metrics"))

  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
