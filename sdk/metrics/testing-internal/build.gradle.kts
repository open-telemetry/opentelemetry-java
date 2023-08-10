plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Metrics Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.metrics.internal.testing")

dependencies {
  api("org.junit.jupiter:junit-jupiter-api")
  implementation("org.slf4j:jul-to-slf4j")
  api(project(":sdk:common"))
  api(project(":sdk:metrics"))
}
