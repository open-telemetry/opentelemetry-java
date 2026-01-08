plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.internal.otlp")

val versions: Map<String, String> by project
dependencies {
  protoSource("io.opentelemetry.proto:opentelemetry-proto:${versions["io.opentelemetry.proto"]}")

  annotationProcessor("com.google.auto.value:auto-value")

  api(project(":exporters:common"))

  compileOnly(project(":sdk:metrics"))
  compileOnly(project(":sdk:trace"))
  compileOnly(project(":sdk:logs"))
  compileOnly(project(":api:incubator"))

  testImplementation(project(":sdk:metrics"))
  testImplementation(project(":sdk:trace"))
  testImplementation(project(":sdk:logs"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.google.guava:guava")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")

  jmhImplementation(project(":api:incubator"))
  jmhImplementation(project(":sdk:testing"))
  jmhImplementation("com.fasterxml.jackson.core:jackson-core")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhImplementation("io.grpc:grpc-netty")
}

testing {
  suites {
    register<JvmTestSuite>("testIncubating") {
      dependencies {
        implementation(project(":api:incubator"))
        implementation(project(":sdk:testing"))

        implementation("com.fasterxml.jackson.core:jackson-databind")
        implementation("com.google.protobuf:protobuf-java-util")
        implementation("com.google.guava:guava")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
      }
    }
  }
}

wire {
  root(
    "opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest",
    "opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest",
    "opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest",
    "opentelemetry.proto.collector.profiles.v1development.ExportProfilesServiceRequest"
  )

  custom {
    schemaHandlerFactoryClass = "io.opentelemetry.gradle.ProtoFieldsWireHandlerFactory"
  }
}

// Configure JMH jar to preserve multi-release jar attribute
// so StringMarshalBenchmark can use VarHandleStringEncoder
tasks.named<Jar>("jmhJar") {
  manifest {
    attributes["Multi-Release"] = "true"
  }
}
