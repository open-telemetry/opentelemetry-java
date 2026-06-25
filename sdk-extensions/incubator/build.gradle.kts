plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Incubator"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.incubator")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator", "io.opentelemetry.sdk.autoconfigure.spi"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
  "io.opentelemetry.sdk.extension.incubator.resources.EntityDetector",
))

dependencies {
  api(project(":sdk:all"))

  annotationProcessor("com.google.auto.value:auto-value")

  compileOnly(project(":api:incubator"))

  compileOnly(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("com.google.guava:guava-testlib")
}
