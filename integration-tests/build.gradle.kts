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
  testImplementation("org.testcontainers:junit-jupiter")
}

tasks {
  withType<Test>().configureEach {
    val denyUnsafe = gradle.startParameter.projectProperties.get("denyUnsafe")?.toBoolean() ?: false
    if (denyUnsafe) {
      // These tests use Armeria which uses Unsafe via JCTools
      jvmArgs("--sun-misc-unsafe-memory-access=allow")
    }
  }
}
