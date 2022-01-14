plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Logs Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.logs")

dependencies {
  api(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))
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
  }
}

tasks {
  named("check") {
    dependsOn(testing.suites)
  }
}
