plugins {
  id("otel.java-conventions")
}

description = "OTLP Exporter Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests.otlp")

dependencies {
  api("org.testcontainers:junit-jupiter")

  implementation(project(":exporters:otlp:all"))
  implementation(project(":exporters:otlp:logs"))
  implementation(project(":semconv"))

  implementation("com.linecorp.armeria:armeria-grpc-protocol")
  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("io.opentelemetry.proto:opentelemetry-proto")
  implementation("org.assertj:assertj-core")
  implementation("org.awaitility:awaitility")
  implementation("org.junit.jupiter:junit-jupiter-params")
}

testing {
  suites {
    val testGrpcJava by registering(JvmTestSuite::class) {
      dependencies {
        runtimeOnly("io.grpc:grpc-netty-shaded")
        runtimeOnly("io.grpc:grpc-stub")
      }
    }
  }
}

tasks {
  // Don't need javadoc.
  javadoc {
    isEnabled = false
  }

  check {
    dependsOn(testing.suites)
  }
}
