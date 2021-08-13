plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")

    id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol HTTP Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.http.trace")

dependencies {
  api(project(":sdk:trace"))

  implementation(project(":exporters:otlp:common"))

  implementation("com.alibaba:fastjson:1.1.72.android")
  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.squareup.okhttp3:okhttp-tls")
  implementation("com.squareup.okio:okio")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
