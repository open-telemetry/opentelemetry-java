plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.incubator")

dependencies {
  api(project(":sdk:all"))

  compileOnly(project(":sdk:trace-shaded-deps"))

  annotationProcessor("com.google.auto.value:auto-value")

  // io.opentelemetry.sdk.extension.incubator.metric.viewconfig
  implementation(project(":sdk-extensions:autoconfigure-spi"))
  implementation("org.snakeyaml:snakeyaml-engine")

  // io.opentelemetry.sdk.extension.trace.zpages
  implementation(project(":semconv"))
  compileOnly("com.sun.net.httpserver:http")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))

  testImplementation("com.google.guava:guava-testlib")
}
