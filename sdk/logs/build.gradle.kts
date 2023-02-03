plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Log SDK"
otelJava.moduleName.set("io.opentelemetry.sdk.logs")

dependencies {
  api(project(":api:logs"))
  api(project(":api:events"))
  api(project(":sdk:common"))

  testImplementation(project(":sdk:logs-testing"))

  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
