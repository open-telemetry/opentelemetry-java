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

  implementation("com.alibaba:fastjson:1.1.72.android")
  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.squareup.okhttp3:okhttp-tls")
  implementation("com.squareup.okio:okio")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
