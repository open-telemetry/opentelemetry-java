plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry Logs SDK Testing utilities"
otelJava.moduleName.set("io.opentelemetry.sdk.logs.testing")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))
  api(project(":sdk:logs"))
  api(project(":sdk:testing"))

  compileOnly("org.assertj:assertj-core")
  compileOnly("junit:junit")
  compileOnly("org.junit.jupiter:junit-jupiter-api")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:logs-testing"))

  testImplementation("junit:junit")
}
