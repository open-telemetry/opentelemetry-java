plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
}

description = "OpenTelemetry - zPages"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.zpages")

dependencies {
  implementation(project(":api:opentelemetry-api"))
  implementation(project(":sdk:opentelemetry-sdk"))

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))

  testImplementation("com.google.guava:guava")

  compileOnly("com.sun.net.httpserver:http")
}
