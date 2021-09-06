plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Extension: Baggage SpanProcessor"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.baggage")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:all"))
  // TODO: Move StringPredicates to shared location.
  api(project(":sdk:metrics"))
}
