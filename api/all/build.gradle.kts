plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API"
otelJava.moduleName.set("io.opentelemetry.api")
base.archivesBaseName = "opentelemetry-api"

dependencies {
  api(project(":context"))

  annotationProcessor("com.google.auto.value:auto-value")

  // this is needed for the fuzz test to run
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("com.google.guava:guava-testlib")
}
