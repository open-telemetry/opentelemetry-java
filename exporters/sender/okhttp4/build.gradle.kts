plugins {
  id("otel.java-conventions")
  // TODO: enable to publish
  // id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp4 Senders"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.okhttp.internal")

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  compileOnly("io.grpc:grpc-stub")
  compileOnly("com.fasterxml.jackson.core:jackson-core")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
