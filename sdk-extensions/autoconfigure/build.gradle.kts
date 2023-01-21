plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Auto-configuration"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))
  api(project(":sdk:logs"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  implementation(project(":semconv"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:trace-shaded-deps"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.guava:guava")
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
}

testing {
  suites {
    val testAutoConfigureOrder by registering(JvmTestSuite::class) {
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
    val testConditionalResourceProvider by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":semconv"))
      }

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
    val testFullConfig by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":extensions:trace-propagators"))
        implementation(project(":exporters:jaeger"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:logging-otlp"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:otlp:logs"))
        implementation(project(":exporters:otlp:common"))
        implementation(project(":exporters:prometheus"))
        implementation(project(":exporters:zipkin"))
        implementation(project(":sdk:testing"))
        implementation(project(":sdk:trace-shaded-deps"))
        implementation(project(":sdk-extensions:jaeger-remote-sampler"))
        implementation(project(":semconv"))

        implementation("com.google.guava:guava")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
        runtimeOnly("io.grpc:grpc-netty-shaded")
      }

      targets {
        all {
          testTask {
            environment("OTEL_LOGS_EXPORTER", "otlp")
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
