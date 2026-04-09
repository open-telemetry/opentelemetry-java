plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Autoconfigure with YAML configuration support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.autoconfigure.config")

dependencies {
  api(project(":sdk-extensions:autoconfigure"))
  runtimeOnly("org.snakeyaml:snakeyaml-engine")
  runtimeOnly("com.fasterxml.jackson.core:jackson-databind")
}
