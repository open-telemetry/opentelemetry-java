plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests")

dependencies {
  implementation(project(":sdk:opentelemetry-sdk"))
  implementation(project(":exporters:opentelemetry-exporter-jaeger"))
  implementation(project(":opentelemetry-semconv"))

  implementation("io.grpc:grpc-protobuf")
  implementation("com.google.protobuf:protobuf-java")
  implementation("io.grpc:grpc-netty-shaded")

  testImplementation(project(":extensions:opentelemetry-extension-trace-propagators"))
  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("org.slf4j:slf4j-simple")
}
