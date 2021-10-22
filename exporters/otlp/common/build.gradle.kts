import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")

  id("com.github.johnrengelman.shadow")
  id("com.squareup.wire")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.internal")

val versions: Map<String, String> by project
dependencies {
  protoSource("io.opentelemetry.proto:opentelemetry-proto:${versions["io.opentelemetry.proto"]}")

  compileOnly(project(":api:all"))
  compileOnly(project(":api:metrics"))

  compileOnly(project(":sdk:metrics"))
  compileOnly(project(":sdk:trace"))
  compileOnly(project(":sdk:logs"))

  // Shaded in
  implementation("com.squareup.okhttp3:okhttp")

  // We include helpers shared by gRPC or okhttp exporters but do not want to impose these
  // dependency on all of our consumers.
  compileOnly("com.fasterxml.jackson.core:jackson-core")
  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")
  compileOnly("io.grpc:grpc-stub")

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:metrics"))
  testImplementation(project(":sdk:trace"))
  testImplementation(project(":sdk:logs"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.fasterxml.jackson.core:jackson-core")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")

  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation(project(":sdk-extensions:resources"))
  jmhImplementation("com.fasterxml.jackson.core:jackson-core")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

wire {
  root(
    "opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest",
    "opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest",
    "opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest"
  )

  custom {
    customHandlerClass = "io.opentelemetry.gradle.ProtoFieldsWireHandler"
  }
}

// The jar dependencies bundled in the uber-jar by the shadow plugin are wrongly added as
// 'runtime' dependencies in the generated pom.xml instead of being absent this pom.xml.
// Remove those runtime dependencies from the pom.xml.
publishing {
  (components["java"] as AdhocComponentWithVariants).run {
    withVariantsFromConfiguration(configurations["runtimeElements"]) {
      skip()
    }
  }
}

tasks {
  val relocateShadowJar by registering(ConfigureShadowRelocation::class) {
    target = shadowJar.get()
    prefix = "io.opentelemetry.exporter.internal.shaded"
  }

  shadowJar {
    dependsOn(relocateShadowJar)

    archiveClassifier.set("")

    minimize()
  }

  jar {
    dependsOn(shadowJar)
    enabled = false
  }
}
