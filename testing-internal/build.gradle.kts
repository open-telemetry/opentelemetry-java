plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.internal.testing")

dependencies {
  api("org.junit.jupiter:junit-jupiter-api")
}
