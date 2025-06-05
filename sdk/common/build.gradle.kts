import io.opentelemetry.gradle.OtelVersionClassPlugin

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
  id("otel.animalsniffer-conventions")
  id("otel.jmh-conventions")
}
apply<OtelVersionClassPlugin>()

description = "OpenTelemetry SDK Common"
otelJava.moduleName.set("io.opentelemetry.sdk.common")

dependencies {
  api(project(":api:all"))
  compileOnly(project(":api:incubator"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation("com.google.guava:guava-testlib")
  testImplementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating")
}

tasks {
  test {
    // For checking version number included in Resource.
    systemProperty("otel.test.project-version", project.version.toString())
  }
}
