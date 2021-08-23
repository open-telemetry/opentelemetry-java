plugins {
  id("otel.java-conventions")

  id("com.github.johnrengelman.shadow")
}

description = "OpenTelemetry W3C Context Propagation Integration Tests"

otelJava.moduleName.set("io.opentelemetry.tracecontext.integration.tests")

dependencies {
  implementation(project(":sdk:all"))
  implementation(project(":extensions:trace-propagators"))

  implementation("com.linecorp.armeria:armeria")
  implementation("org.slf4j:slf4j-simple")

  testImplementation("org.testcontainers:junit-jupiter")
}

tasks {
  val shadowJar by existing(Jar::class) {
    archiveFileName.set("tracecontext-tests.jar")

    manifest { attributes("Main-Class" to "io.opentelemetry.Application") }
  }

  withType(Test::class) {
    dependsOn(shadowJar)

    jvmArgs(
      "-Dio.opentelemetry.testArchive=${shadowJar.get().archiveFile.get().asFile.absolutePath}"
    )
  }
}
