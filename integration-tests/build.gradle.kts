plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests")

dependencies {
  implementation(project(":sdk:all"))
  implementation(project(":exporters:jaeger"))
  implementation(project(":semconv"))

  implementation("io.grpc:grpc-protobuf")
  implementation("com.google.protobuf:protobuf-java")
  implementation("io.grpc:grpc-netty-shaded")

  testImplementation(project(":extensions:trace-propagators"))
  testImplementation(project(":sdk:testing"))
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("org.slf4j:slf4j-simple")
}
