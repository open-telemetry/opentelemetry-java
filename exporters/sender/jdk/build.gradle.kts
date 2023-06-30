plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry JDK HttpSender"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.jdk.internal")

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))
}

tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.release.set(11)
  }
}
