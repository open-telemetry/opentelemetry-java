import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry JDK HttpSender"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.jdk.internal")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_11)

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  testImplementation("com.fasterxml.jackson.core:jackson-core")
}
