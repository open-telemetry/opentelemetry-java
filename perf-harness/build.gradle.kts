plugins {
  id("otel.java-conventions")
}

description = "Performance Testing Harness"
otelJava.moduleName.set("io.opentelemetry.perf-harness")

dependencies {
  implementation(project(":api:all"))
  implementation(project(":sdk:all"))
  implementation(project(":sdk:testing"))
  implementation(project(":exporters:otlp:all"))
  implementation(project(":exporters:logging"))

  implementation("eu.rekawek.toxiproxy:toxiproxy-java")
  implementation("org.testcontainers:testcontainers-junit-jupiter")

  runtimeOnly("io.grpc:grpc-netty-shaded")
}
