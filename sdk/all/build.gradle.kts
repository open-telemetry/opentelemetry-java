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
  api(project(":api:all"))
  api(project(":sdk:common"))
  api(project(":sdk:metrics"))
  api(project(":sdk:trace"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
}
