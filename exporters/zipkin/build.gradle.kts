plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Zipkin Exporter"

otelJava.moduleName.set("io.opentelemetry.exporter.zipkin")

dependencies {
  compileOnly("com.google.auto.value:auto-value")

  api(project(":sdk:all"))

  api("io.zipkin.reporter2:zipkin-reporter")

  annotationProcessor("com.google.auto.value:auto-value")

  implementation(project(":semconv"))

  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3")

  testImplementation(project(":sdk:testing"))

  testImplementation("io.zipkin.zipkin2:zipkin-junit")
}
