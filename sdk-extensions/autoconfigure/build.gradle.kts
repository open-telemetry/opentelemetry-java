plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Auto-configuration"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  implementation(project(":api:incubator"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:trace-shaded-deps"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.guava:guava")
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
}

testing {
  suites {
    register<JvmTestSuite>("testAutoConfigureOrder") {
      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
            environment("OTEL_LOGS_EXPORTER", "none")
          }
        }
      }
    }
    register<JvmTestSuite>("testConditionalResourceProvider") {
      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
            environment("OTEL_LOGS_EXPORTER", "none")
          }
        }
      }
    }
    register<JvmTestSuite>("testFullConfig") {
      dependencies {
        implementation(project(":api:incubator"))
        implementation(project(":extensions:trace-propagators"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:logging-otlp"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:prometheus"))
        implementation("io.prometheus:prometheus-metrics-exporter-httpserver")
        implementation(project(":exporters:zipkin"))
        implementation(project(":sdk:testing"))
        implementation(project(":sdk:trace-shaded-deps"))
        implementation(project(":sdk-extensions:jaeger-remote-sampler"))
        implementation(project(":sdk-extensions:incubator"))

        implementation("com.google.guava:guava")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
      }

      targets {
        all {
          testTask {
            environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=test,cat=meow")
            environment("OTEL_PROPAGATORS", "tracecontext,baggage,b3,b3multi,jaeger,ottrace,test")
            environment("OTEL_EXPORTER_OTLP_HEADERS", "cat=meow,dog=bark")
            environment("OTEL_EXPORTER_OTLP_TIMEOUT", "5000")
            environment("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT", "2")
            environment("OTEL_TEST_CONFIGURED", "true")
            environment("OTEL_TEST_WRAPPED", "1")
          }
        }
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
