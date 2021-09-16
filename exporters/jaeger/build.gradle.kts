plugins {
  id("otel.java-conventions")
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry - Jaeger Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.jaeger")

dependencies {
  api(project(":sdk:all"))
  api("io.grpc:grpc-api")

  // TODO(anuraaga): otlp-common has a lot of code not specific to OTLP now. As it's just internal
  // code, this mysterious dependency is possibly still OK but we may need a rename or splitting.
  implementation(project(":exporters:otlp:common"))
  implementation(project(":semconv"))

  implementation("io.grpc:grpc-stub")
  implementation("com.fasterxml.jackson.jr:jackson-jr-objects")

  testImplementation("com.fasterxml.jackson.jr:jackson-jr-stree")
  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("org.testcontainers:junit-jupiter")

  testImplementation(project(":sdk:testing"))

  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}

wire {
  custom {
    customHandlerClass = "io.opentelemetry.gradle.ProtoFieldsWireHandler"
  }
}

// We only use the generated protos in tests
sourceSets {
  test {
    proto {
      srcDir("src/main/proto")
    }
  }
}
afterEvaluate {
  tasks {
    named("generateProto") {
      enabled = false
    }
  }
}
