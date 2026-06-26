import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry JSON Jackson 3 Provider"
otelJava.moduleName.set("io.opentelemetry.json.jackson3")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_17)
otelJava.osgiServiceLoaderProvides.set(listOf("io.opentelemetry.sdk.common.export.JsonProvider"))

base.archivesName.set("opentelemetry-json-jackson-3")

dependencies {
  api(project(":sdk:common"))

  implementation("tools.jackson.core:jackson-core")
}
