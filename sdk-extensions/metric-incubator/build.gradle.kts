plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Metric Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.metric.incubator")

dependencies {
  implementation(project(":sdk:metrics"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  implementation("org.yaml:snakeyaml")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))
  testImplementation(project(":sdk:metrics-testing"))
}
