plugins {
  id("otel.java-conventions")
  // TODO (jack-berg): uncomment when ready to publish
  // id("otel.publish-conventions")

  // animalsniffer is disabled on this module to allow use of the JFR API.
  // id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profiles Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.profiles")

val versions: Map<String, String> by project

tasks {
  // this module uses the jdk.jfr.consumer API, which was backported into 1.8 but is '@since 9'
  // and therefore a bit of a pain to get gradle to compile against...
  compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(null as Int?)
  }
  compileTestJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(null as Int?)
  }
}

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
