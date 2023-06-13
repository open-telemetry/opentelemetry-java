plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp HttpSender"
otelJava.moduleName.set("io.opentelemetry.exporter.http.okhttp.internal")

val versions: Map<String, String> by project
dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("com.squareup.okhttp3:okhttp")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
