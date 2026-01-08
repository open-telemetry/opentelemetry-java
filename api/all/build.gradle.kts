plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API"
otelJava.moduleName.set("io.opentelemetry.api")
base.archivesName.set("opentelemetry-api")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.sdk.autoconfigure", "io.opentelemetry.api.incubator"))

dependencies {
  api(project(":context"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":api:testing-internal"))

  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("com.google.guava:guava-testlib")
}

tasks.test {
  // Configure environment variable for ConfigUtilTest
  environment("CONFIG_KEY", "environment")
}
