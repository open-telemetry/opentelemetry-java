plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.internal")

dependencies {
  api(project(":api:all"))
  api(project(":proto"))
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))

  implementation("com.google.protobuf:protobuf-java")

  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation(project(":sdk-extensions:resources"))
}
