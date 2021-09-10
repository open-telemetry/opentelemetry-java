import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK For Tracing"
otelJava.moduleName.set("io.opentelemetry.sdk.trace")

sourceSets {
  main {
    val traceShadedDeps = project(":sdk:sdk-trace-shaded-deps")
    output.dir(traceShadedDeps.file("build/extracted/shadow"), "builtBy" to ":sdk:sdk-trace-shaded-deps:extractShadowJar")
  }
}

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk-common"))

  compileOnly(project(":sdk:sdk-trace-shaded-deps"))

  implementation(project(":api:opentelemetry-api-metrics"))
  implementation(project(":opentelemetry-semconv"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation("com.google.guava:guava")

  jmh(project(":sdk:opentelemetry-sdk-metrics"))
  jmh(project(":sdk:opentelemetry-sdk-testing")) {
    // JMH doesn"t handle dependencies that are duplicated between the main and jmh
    // configurations properly, but luckily here it"s simple enough to just exclude transitive
    // dependencies.
    isTransitive = false
  }
  jmh(project(":exporters:opentelemetry-exporter-jaeger-thrift"))
  jmh(project(":exporters:otlp:opentelemetry-exporter-otlp-trace")) {
    // The opentelemetry-exporter-otlp-trace depends on this project itself. So don"t pull in
    // the transitive dependencies.
    isTransitive = false
  }
  // explicitly adding the opentelemetry-exporter-otlp dependencies
  jmh(project(":exporters:otlp:opentelemetry-exporter-otlp-common")) {
    isTransitive = false
  }
  jmh(project(":opentelemetry-proto"))

  jmh("com.google.guava:guava")
  jmh("io.grpc:grpc-api")
  jmh("io.grpc:grpc-netty-shaded")
  jmh("org.testcontainers:testcontainers") // testContainer for OTLP collector
}

tasks {
  withType<AnimalSniffer>().configureEach {
    // We catch NoClassDefFoundError to fallback to non-jctools queues.
    exclude("**/internal/shaded/jctools/**")
    exclude("**/internal/JcTools*")
  }
}
