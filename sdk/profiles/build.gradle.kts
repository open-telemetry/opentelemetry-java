plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profiles SDK"
otelJava.moduleName.set("sdk.profiles")

dependencies {
  api(project(":sdk:common"))

  annotationProcessor("com.google.auto.value:auto-value")
}
