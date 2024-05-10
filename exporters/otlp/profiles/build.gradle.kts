plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Profiling SDK"
otelJava.moduleName.set("io.opentelemetry.sdk.profiles")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))

  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
