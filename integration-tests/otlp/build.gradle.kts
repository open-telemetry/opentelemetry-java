plugins {
  id("otel.java-conventions")
}

description = "OTLP Exporter Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests.otlp")

dependencies {
  api("org.testcontainers:testcontainers-junit-jupiter")

  implementation(project(":exporters:otlp:all"))
  implementation(project(":api:incubator"))

  compileOnly("com.google.errorprone:error_prone_annotations")

  implementation("com.linecorp.armeria:armeria-grpc-protocol")
  implementation("com.linecorp.armeria:armeria-junit5")
  implementation("io.opentelemetry.proto:opentelemetry-proto")
  implementation("org.assertj:assertj-core")
  implementation("org.awaitility:awaitility")
  implementation("org.junit.jupiter:junit-jupiter-params")
}

testing {
  suites {
    register<JvmTestSuite>("testGrpcJava") {
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

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}
