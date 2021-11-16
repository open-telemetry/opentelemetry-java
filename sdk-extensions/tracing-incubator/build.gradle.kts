plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Tracing Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.incubator")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))

  compileOnly(project(":sdk:trace-shaded-deps"))

  implementation(project(":api:metrics"))
  implementation(project(":semconv"))

  annotationProcessor("com.google.auto.value:auto-value")
  testImplementation(project(":sdk:testing"))
  testImplementation("com.google.guava:guava-testlib")
}
