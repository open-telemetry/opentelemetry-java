plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Incubator"
otelJava.moduleName.set("io.opentelemetry.api.incubator")

dependencies {
  api(project(":api:all"))

  // Supports optional InstrumentationConfigUtil#convertToModel
  compileOnly("com.fasterxml.jackson.core:jackson-databind")

  annotationProcessor("com.google.auto.value:auto-value")

  // To use parsed config file as input for InstrumentationConfigUtilTest
  testImplementation(project(":sdk-extensions:incubator"))

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":api:testing-internal"))

  testImplementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating")

  testImplementation("org.slf4j:slf4j-api")
  testImplementation("org.apache.logging.log4j:log4j-api:2.25.2")

  testImplementation("com.google.guava:guava")
}

testing {
  suites {
    register<JvmTestSuite>("testConvertToModel") {
      dependencies {
        implementation("com.fasterxml.jackson.core:jackson-databind")
        implementation(project(":sdk-extensions:incubator"))
        implementation(project(":sdk-extensions:autoconfigure"))
        implementation("com.google.guava:guava")
      }
    }
  }
}
