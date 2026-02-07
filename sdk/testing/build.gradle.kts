plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Testing utilities"
otelJava.moduleName.set("io.opentelemetry.sdk.testing")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))
  compileOnly(project(":api:incubator"))

  compileOnly("org.assertj:assertj-core")
  compileOnly("junit:junit")
  compileOnly("org.junit.jupiter:junit-jupiter-api")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":api:incubator"))

  testImplementation("junit:junit")
  testImplementation("org.junit.vintage:junit-vintage-engine")
}

testing {
  suites {
    register<JvmTestSuite>("testIncubating") {
      dependencies {
        implementation(project(":api:incubator"))
      }
    }
  }
}

tasks {
  // TestExtendedLogRecordData generated AutoValue class imports deprecated ExtendedAttributes.
  // @SuppressWarnings can't suppress import statement warnings.
  // TODO: remove after removing ExtendedAttributes
  named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Xlint:-deprecation")
  }
}
