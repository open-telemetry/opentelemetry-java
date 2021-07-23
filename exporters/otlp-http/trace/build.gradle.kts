plugins {
    id("otel.java-conventions")
    // TODO: uncomment once ready to publish
    // id("otel.publish-conventions")

    id("otel.animalsniffer-conventions")
    id("com.github.johnrengelman.shadow")
}

description = "OpenTelemetry Protocol HTTP Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.http.trace")

dependencies {
  api(project(":sdk:trace"))

  implementation(project(":exporters:otlp:common"))

  implementation("com.squareup.okhttp3:okhttp")
  implementation("com.squareup.okhttp3:okhttp-tls")
  implementation("com.squareup.okio:okio")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.squareup.okhttp3:mockwebserver")
}

tasks {
  shadowJar {
    minimize()

    relocate("okhttp3", "io.opentelemetry.internal.shaded.okhttp3")
    relocate("okio", "io.opentelemetry.internal.shaded.okio")
  }
}
