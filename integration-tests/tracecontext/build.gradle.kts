plugins {
  id("otel.java-conventions")

  id("com.gradleup.shadow")
}

description = "OpenTelemetry W3C Context Propagation Integration Tests"
otelJava.moduleName.set("io.opentelemetry.tracecontext.integration.tests")

dependencies {
  implementation(project(":sdk:all"))
  implementation(project(":extensions:trace-propagators"))

  compileOnly("com.google.errorprone:error_prone_annotations")

  implementation("com.linecorp.armeria:armeria")

  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}

tasks {
  val shadowJar by existing(Jar::class) {
    archiveFileName.set("tracecontext-tests.jar")

    manifest {
      attributes("Main-Class" to "io.opentelemetry.Application")
    }
  }

  withType(Test::class) {
    dependsOn(shadowJar)

    jvmArgs("-Dio.opentelemetry.testArchive=${shadowJar.get().archiveFile.get().asFile.absolutePath}")
  }
}

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}
