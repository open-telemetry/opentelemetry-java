plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Common"
otelJava.moduleName.set("io.opentelemetry.common")

dependencies {
}
