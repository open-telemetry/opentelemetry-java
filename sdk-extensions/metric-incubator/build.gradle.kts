plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Metric Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.metric.incubator")

dependencies {
  implementation(project(":sdk:metrics"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  implementation("com.fasterxml.jackson.core:jackson-core")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))
  testImplementation(project(":sdk:metrics-testing"))
}
