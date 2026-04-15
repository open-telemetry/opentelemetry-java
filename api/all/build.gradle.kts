plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API"
otelJava.moduleName.set("io.opentelemetry.api")
base.archivesName.set("opentelemetry-api")
// These packages cannot be compileOnly dependencies (api:incubator depends on api:all, creating a
// circular dependency; sdk:autoconfigure is in a different module family). Declare them as optional
// imports without a version constraint so OSGi wires them if present but does not require them.
otelJava.osgiUnversionedOptionalPackages.set(listOf("io.opentelemetry.sdk.autoconfigure", "io.opentelemetry.api.incubator", "io.opentelemetry.api.incubator.internal"))

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
