plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp5 Senders"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.okhttp.internal")

tasks.named<Test>("test") {
  systemProperty("expectedOkHttpMajorVersion", "5")
}

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("com.squareup.okhttp3:okhttp:5.3.2")

  compileOnly("io.grpc:grpc-stub")
  compileOnly("com.fasterxml.jackson.core:jackson-core")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
