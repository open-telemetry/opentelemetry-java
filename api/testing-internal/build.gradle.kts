plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry API Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.api.testing.internal")
otelJava.osgiEnabled.set(false)

dependencies {
  api(project(":api:all"))

  implementation(project(":testing-internal"))

  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("org.assertj:assertj-core")
  implementation("org.mockito:mockito-core")
}

// Skip ossIndexAudit on test module
tasks.named("ossIndexAudit") {
  enabled = false
}
