plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK"
otelJava.moduleName.set("io.opentelemetry.sdk")
base.archivesName.set("opentelemetry-sdk")

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk-common"))
  api(project(":sdk:opentelemetry-sdk-trace"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
}
