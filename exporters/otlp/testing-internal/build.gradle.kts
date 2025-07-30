plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Exporter Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.testing.internal")

dependencies {
  api(project(":exporters:otlp:common"))
  api(project(":sdk:logs"))
  api(project(":sdk:metrics"))
  api(project(":sdk:trace"))
  api(project(":sdk:testing"))

  // ugly, but profiles doesn't have a separate API yet.
  api(project(":exporters:otlp:profiles"))

  api(project(":exporters:otlp:all"))

  // Must be compileOnly so gRPC isn't on the classpath for non-gRPC tests.
  compileOnly("io.grpc:grpc-stub")
  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")

  implementation(project(":testing-internal"))

  api("io.opentelemetry.proto:opentelemetry-proto")
  api("org.junit.jupiter:junit-jupiter-api")
  implementation("com.squareup.okhttp3:okhttp-jvm")
  implementation("org.junit.jupiter:junit-jupiter-params")

  implementation("com.linecorp.armeria:armeria-grpc-protocol")
  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("io.github.netmikey.logunit:logunit-jul")
  implementation("org.assertj:assertj-core")
  implementation("org.mock-server:mockserver-netty")
}

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}
