plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol JSON Logging Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.logging.otlp")

dependencies {
  compileOnly(project(":sdk:trace"))
  compileOnly(project(":sdk:metrics"))
  compileOnly(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))

  implementation("com.fasterxml.jackson.core:jackson-core")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk:logs"))
  testImplementation(project(":sdk:metrics"))
  testImplementation(project(":sdk:trace"))

  testImplementation("org.skyscreamer:jsonassert")
}
