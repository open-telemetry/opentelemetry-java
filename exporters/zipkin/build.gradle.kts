plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Zipkin Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.zipkin")

dependencies {
  api(project(":sdk:all"))

  api("io.zipkin.reporter2:zipkin-reporter")

  implementation(project(":semconv"))

  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.linecorp.armeria:armeria")
  testImplementation("org.testcontainers:junit-jupiter")
}
