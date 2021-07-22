plugins {
  id("otel.java-conventions")

  id("com.github.johnrengelman.shadow")
}

// This project is not published, it is bundled into :exporters:otlp:trace

description = "Internal use only - shaded dependencies of OpenTelemetry SDK for OTLP Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.internal")

dependencies {
  api("com.squareup.okhttp3:okhttp")
  api("com.squareup.okhttp3:okhttp-tls")
  api("com.squareup.okio:okio")
}

tasks {
  shadowJar {
    minimize()

    relocate("okhttp3", "io.opentelemetry.internal.shaded.okhttp3")
    relocate("okio", "io.opentelemetry.internal.shaded.okio")
  }
}
