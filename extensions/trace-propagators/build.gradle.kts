plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
  id("otel.jmh-conventions")
}

description = "OpenTelemetry Extension : Trace Propagators"
otelJava.moduleName.set("io.opentelemetry.extension.trace.propagation")

dependencies {
  api(project(":api:all"))

  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation("io.jaegertracing:jaeger-client")
  testImplementation("com.google.guava:guava")
}
