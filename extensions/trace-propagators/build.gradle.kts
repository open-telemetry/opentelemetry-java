plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
  id("otel.jmh-conventions")
}

description = "OpenTelemetry Extension : Trace Propagators"
otelJava.moduleName.set("io.opentelemetry.extension.trace.propagation")
otelJava.osgiOptionalPackages.set(listOf("io.opentelemetry.api.incubator"))
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider",
  "io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider",
))

dependencies {
  api(project(":api:all"))

  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation("io.jaegertracing:jaeger-client")
  testImplementation("com.google.guava:guava")
}
