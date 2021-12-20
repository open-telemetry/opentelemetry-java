plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry - Jaeger Remote sampler"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.jaeger")

dependencies {
  api(project(":sdk:all"))
  compileOnly(project(":sdk-extensions:autoconfigure"))

  implementation(project(":sdk:all"))

  // TODO(anuraaga): otlp-common has a lot of code not specific to OTLP now. As it's just internal
  // code, this mysterious dependency is possibly still OK but we may need a rename or splitting.
  implementation(project(":exporters:otlp:common"))
  compileOnly("io.grpc:grpc-stub")

  api("com.google.protobuf:protobuf-java")
  compileOnly("io.grpc:grpc-api")
  compileOnly("io.grpc:grpc-protobuf")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))

  testImplementation("io.grpc:grpc-testing")
  testImplementation("org.testcontainers:junit-jupiter")

  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}

wire {
  custom {
    customHandlerClass = "io.opentelemetry.gradle.ProtoFieldsWireHandler"
  }
}
