plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Jaeger Remote sampler"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.jaeger")

dependencies {
  api(project(":sdk:opentelemetry-sdk"))
  compileOnly(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure"))

  implementation(project(":sdk:opentelemetry-sdk"))
  implementation("io.grpc:grpc-api")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("com.google.protobuf:protobuf-java")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure"))

  testImplementation("io.grpc:grpc-testing")
  testImplementation("org.testcontainers:junit-jupiter")

  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
