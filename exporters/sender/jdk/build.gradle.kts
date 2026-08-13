import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry JDK HttpSender"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.jdk.internal")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_11)
otelJava.osgiServiceLoaderProvides.set(listOf("io.opentelemetry.sdk.common.export.HttpSenderProvider"))

dependencies {
  annotationProcessor("com.google.auto.value:auto-value")

  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))
}
