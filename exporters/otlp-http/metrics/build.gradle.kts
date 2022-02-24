plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol HTTP Metrics Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.http.metrics")

dependencies {
  api(project(":sdk:metrics"))

  implementation(project(":exporters:otlp:common"))

  implementation("com.squareup.okhttp3:okhttp")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.squareup.okhttp3:okhttp-tls")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
}
