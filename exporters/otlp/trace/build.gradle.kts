plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
  id("otel.jmh-conventions")
}

description = "OpenTelemetry Protocol Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.trace")

dependencies {
  api(project(":sdk:trace"))
  api(project(":exporters:otlp:common"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation("com.linecorp.armeria:armeria")
  jmhImplementation("com.linecorp.armeria:armeria-grpc")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("com.squareup.okhttp3:okhttp")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

testing {
  suites {
    val testGrpcNetty by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-netty")
        implementation("io.grpc:grpc-stub")
      }
    }
    val testGrpcNettyShaded by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-netty-shaded")
        implementation("io.grpc:grpc-stub")
      }
    }
    val testGrpcOkhttp by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-okhttp")
        implementation("io.grpc:grpc-stub")
      }
    }
    val testSpanPipeline by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.grpc:grpc-protobuf")
        implementation("io.grpc:grpc-testing")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
      }
    }
  }
}

tasks {
  check {
    dependsOn(
      testing.suites.filter {
        // Mainly to conveniently profile through IDEA. Don't add to check task, it's for
        // manual invocation.
        name != "testSpanPipeline"
      }
    )
  }
}
