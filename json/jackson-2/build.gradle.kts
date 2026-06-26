plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry JSON Jackson 2 Provider"
otelJava.moduleName.set("io.opentelemetry.json.jackson2")
otelJava.osgiServiceLoaderProvides.set(listOf("io.opentelemetry.sdk.common.export.JsonProvider"))

base.archivesName.set("opentelemetry-json-jackson-2")

dependencies {
  api(project(":sdk:common"))

  implementation("com.fasterxml.jackson.core:jackson-core")
}
