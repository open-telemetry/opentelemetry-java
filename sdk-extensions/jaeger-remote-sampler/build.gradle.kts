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

  implementation("com.squareup.okhttp3:okhttp")

  compileOnly("io.grpc:grpc-api")
  compileOnly("io.grpc:grpc-protobuf")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))

  testImplementation("com.fasterxml.jackson.jr:jackson-jr-stree")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc-protocol")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("io.grpc:grpc-testing")
}

wire {
  custom {
    customHandlerClass = "io.opentelemetry.gradle.ProtoFieldsWireHandler"
  }
}

sourceSets {
  main {
    java.srcDir("$buildDir/generated/source/wire")
  }
}
