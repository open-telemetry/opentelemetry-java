plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Incubator"
otelJava.moduleName.set("io.opentelemetry.api.incubator")

dependencies {
  api(project(":api:all"))

  annotationProcessor("com.google.auto.value:auto-value")

  // To use parsed config file as input for InstrumentationConfigUtilTest
  testImplementation(project(":sdk-extensions:incubator"))

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":api:testing-internal"))

  testImplementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating")

  testImplementation("com.google.guava:guava")
}
