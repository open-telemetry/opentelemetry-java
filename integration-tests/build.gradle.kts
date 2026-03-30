plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests")

dependencies {
  testImplementation(project(":sdk:all"))
  testImplementation(project(":sdk:testing"))
  testImplementation(project(":extensions:trace-propagators"))

  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}

// Skip ossIndexAudit: the scan plugin traverses Gradle subprojects at execution time, which
// Gradle 9 forbids without an exclusive lock. Each subproject runs its own audit independently.
tasks.named("ossIndexAudit") {
  enabled = false
}
