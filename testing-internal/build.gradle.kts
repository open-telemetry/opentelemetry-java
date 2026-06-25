plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.internal.testing")
otelJava.osgiEnabled.set(false)

dependencies {
  api("org.junit.jupiter:junit-jupiter-api")
  implementation("org.slf4j:jul-to-slf4j")
}
