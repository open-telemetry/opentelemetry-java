plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Testing utilities"
otelJava.moduleName.set("io.opentelemetry.sdk.testing")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))

  compileOnly("org.assertj:assertj-core")
  compileOnly("junit:junit")
  compileOnly("org.junit.jupiter:junit-jupiter-api")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation("junit:junit")
  testImplementation("org.junit.vintage:junit-vintage-engine")
}
