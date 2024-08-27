plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
  id("java-test-fixtures")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API"
otelJava.moduleName.set("io.opentelemetry.api")
base.archivesName.set("opentelemetry-api")

dependencies {
  api(project(":context"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("com.google.guava:guava-testlib")
  testFixturesApi(project(":testing-internal"))
  testFixturesApi("junit:junit")
  testFixturesApi("org.assertj:assertj-core")
  testFixturesApi("org.mockito:mockito-core")
}

tasks.test {
  // Configure environment variable for ConfigUtilTest
  environment("CONFIG_KEY", "environment")
}
