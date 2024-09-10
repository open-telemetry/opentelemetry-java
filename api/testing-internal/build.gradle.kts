plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry API Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.api.testing.internal")

dependencies {
  api(project(":api:all"))

  implementation(project(":testing-internal"))

  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("org.assertj:assertj-core")
  implementation("org.mockito:mockito-core")
}

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}
