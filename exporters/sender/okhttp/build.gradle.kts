plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp Senders"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.okhttp.internal")

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("com.squareup.okhttp3:okhttp")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
