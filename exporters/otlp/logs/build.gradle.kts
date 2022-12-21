plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol (OTLP) Log Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.logs")

dependencies {
  api(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))
  testImplementation(project(":sdk:logs-testing"))

  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("com.squareup.okhttp3:okhttp-tls")
}

testing {
  suites {
    val testGrpcNetty by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))
        implementation(project(":sdk:logs-testing"))

        implementation("io.grpc:grpc-netty")
        implementation("io.grpc:grpc-stub")
      }
    }
    val testGrpcNettyShaded by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))
        implementation(project(":sdk:logs-testing"))

        implementation("io.grpc:grpc-netty-shaded")
        implementation("io.grpc:grpc-stub")
      }
    }
    val testGrpcOkhttp by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:testing-internal"))
        implementation(project(":sdk:logs-testing"))

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
