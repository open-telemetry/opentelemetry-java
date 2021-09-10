plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Extensions for AWS"
otelJava.moduleName.set("io.opentelemetry.extension.aws")

dependencies {
  api(project(":api:opentelemetry-api"))
  compileOnly(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure"))
}
