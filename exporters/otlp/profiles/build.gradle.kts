plugins {
  id("otel.java-conventions")
  // TODO (jack-berg): uncomment when ready to publish
  // id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profiles Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.profiles")

val versions: Map<String, String> by project
dependencies {
  api(project(":sdk:common"))
  api(project(":exporters:common"))
  implementation(project(":exporters:otlp:common"))

  implementation(project(":exporters:otlp:all"))
  compileOnly("io.grpc:grpc-stub")

  annotationProcessor("com.google.auto.value:auto-value")

  testCompileOnly("com.google.guava:guava")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testImplementation(project(":exporters:otlp:testing-internal"))
  testImplementation(project(":exporters:sender:okhttp"))
  testImplementation("io.grpc:grpc-stub")
}
