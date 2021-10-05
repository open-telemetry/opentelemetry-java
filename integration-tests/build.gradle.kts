plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests")

dependencies {
  testImplementation(project(":sdk:all"))
  testImplementation(project(":sdk:testing"))
  testImplementation(project(":extensions:trace-propagators"))
  testImplementation(project(":exporters:jaeger"))
  testImplementation(project(":exporters:otlp:trace"))
  testImplementation(project(":exporters:otlp:metrics"))
  testImplementation(project(":exporters:otlp:logs"))
  testImplementation(project(":exporters:otlp-http:trace"))
  testImplementation(project(":exporters:otlp-http:metrics"))
  testImplementation(project(":exporters:otlp-http:logs"))
  testImplementation(project(":semconv"))
  testImplementation(project(":proto"))

  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("io.grpc:grpc-netty-shaded")

  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.linecorp.armeria:armeria-grpc")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("org.slf4j:slf4j-simple")
}
