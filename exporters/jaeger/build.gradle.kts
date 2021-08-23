plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Jaeger Exporter"

otelJava.moduleName.set("io.opentelemetry.exporter.jaeger")

dependencies {
  api(project(":sdk:all"))
  api("io.grpc:grpc-api")

  implementation(project(":sdk:all"))
  implementation(project(":semconv"))

  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("com.google.protobuf:protobuf-java")
  implementation("com.google.protobuf:protobuf-java-util")

  testImplementation("io.grpc:grpc-testing")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.squareup.okhttp3:okhttp")

  testImplementation(project(":sdk:testing"))

  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}
