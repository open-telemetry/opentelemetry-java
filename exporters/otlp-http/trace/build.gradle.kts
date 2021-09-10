plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol HTTP Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.http.trace")

dependencies {
  api(project(":sdk:opentelemetry-sdk-trace"))

  implementation(project(":exporters:otlp:opentelemetry-exporter-otlp-common"))

  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.squareup.okhttp3:okhttp-tls")
  implementation("com.squareup.okio:okio")

  testImplementation(project(":opentelemetry-proto"))
  testImplementation(project(":sdk:opentelemetry-sdk-testing"))

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("com.linecorp.armeria:armeria-junit5")
}
