plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry - Jaeger Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.jaeger")

dependencies {
  api(project(":sdk:all"))

  protoSource(project(":exporters:jaeger-proto"))

  implementation(project(":exporters:common"))
  implementation(project(":semconv"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly("io.grpc:grpc-stub")

  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.fasterxml.jackson.jr:jackson-jr-objects")

  testImplementation(project(":exporters:jaeger-proto"))

  testImplementation("com.fasterxml.jackson.jr:jackson-jr-stree")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc-protocol")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("org.testcontainers:junit-jupiter")

  testImplementation(project(":sdk:testing"))
}

wire {
  custom {
    schemaHandlerFactoryClass = "io.opentelemetry.gradle.ProtoFieldsWireHandlerFactory"
  }
}

afterEvaluate {
  tasks.getByName("generateMainProtos") {
    setDependsOn(configurations.getByName("protoPath"))
  }
}

// Declare sourcesJar dependency on proto generation so gradle doesn't complain about implicit dependency
tasks.getByName("sourcesJar").dependsOn("generateMainProtos")

sourceSets {
  main {
    java.srcDir("$buildDir/generated/source/wire")
  }
}
