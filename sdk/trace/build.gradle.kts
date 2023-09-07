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
    val traceShadedDeps = project(":sdk:trace-shaded-deps")
    output.dir(traceShadedDeps.file("build/extracted/shadow"), "builtBy" to ":sdk:trace-shaded-deps:extractShadowJar")
  }
}

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))

  compileOnly(project(":sdk:trace-shaded-deps"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation("com.google.guava:guava")
  testImplementation("com.google.guava:guava-testlib")

  jmh(project(":sdk:metrics"))
  jmh(project(":sdk:testing")) {
    // JMH doesn"t handle dependencies that are duplicated between the main and jmh
    // configurations properly, but luckily here it"s simple enough to just exclude transitive
    // dependencies.
    isTransitive = false
  }
  jmh(project(":exporters:jaeger-thrift"))
  jmh(project(":exporters:otlp:all")) {
    // The opentelemetry-exporter-otlp depends on this project itself. So don't pull in
    // the transitive dependencies.
    isTransitive = false
  }
  // explicitly adding the opentelemetry-exporter-otlp dependencies
  jmh(project(":exporters:otlp:common")) {
    isTransitive = false
  }
  jmh("io.opentelemetry.proto:opentelemetry-proto")

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
