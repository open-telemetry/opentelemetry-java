plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry Exporter Testing (Internal)"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.testing.internal")

dependencies {
  api(project(":sdk:trace"))
  api(project(":exporters:otlp:common"))

  implementation(project(":api:metrics"))
  implementation(project(":sdk:testing"))

  api("io.opentelemetry.proto:opentelemetry-proto")
  api("org.junit.jupiter:junit-jupiter-api")

  implementation("com.linecorp.armeria:armeria-grpc-protocol")
  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("io.github.netmikey.logunit:logunit-jul")
  implementation("org.assertj:assertj-core")
  runtimeOnly("org.slf4j:slf4j-simple")
}
