plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Jaeger Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.jaeger")

dependencies {
  api(project(":sdk:opentelemetry-sdk"))
  api("io.grpc:grpc-api")

  implementation(project(":sdk:opentelemetry-sdk"))
  implementation(project(":opentelemetry-semconv"))

  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("com.fasterxml.jackson.jr:jackson-jr-objects")
  implementation("com.google.protobuf:protobuf-java")

  testImplementation("com.fasterxml.jackson.jr:jackson-jr-stree")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.squareup.okhttp3:okhttp")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))

  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
