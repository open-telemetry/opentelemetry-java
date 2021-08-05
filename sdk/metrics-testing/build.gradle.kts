plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Testing utilities"
otelJava.moduleName.set("io.opentelemetry.sdk.metrics-testing")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))
  api(project(":sdk:testing"))

  compileOnly("org.assertj:assertj-core")
  compileOnly("junit:junit")
  compileOnly("org.junit.jupiter:junit-jupiter-api")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation("junit:junit")
}
