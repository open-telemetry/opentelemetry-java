plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry OpenTracing Bridge"
otelJava.moduleName.set("io.opentelemetry.opentracingshim")

dependencies {
  api(project(":api:all"))

  api("io.opentracing:opentracing-api")
  implementation(project(":semconv"))
  implementation("io.opentracing:opentracing-noop:0.33.0")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
}

tasks {
  withType(Test::class) {
    testLogging {
      showStandardStreams = true
    }
  }
}
