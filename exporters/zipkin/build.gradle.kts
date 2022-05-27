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

  // TODO(anuraaga): otlp-common has a lot of code not specific to OTLP now. As it's just internal
  // code, this mysterious dependency is possibly still OK but we may need a rename or splitting.
  implementation(project(":exporters:otlp:common"))
  implementation(project(":semconv"))

  implementation("io.zipkin.reporter2:zipkin-sender-okhttp3")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.linecorp.armeria:armeria")
  testImplementation("org.testcontainers:junit-jupiter")
}
