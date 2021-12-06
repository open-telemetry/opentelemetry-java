plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Auto-configuration"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  implementation(project(":semconv"))

  compileOnly(project(":exporters:jaeger"))
  compileOnly(project(":exporters:logging"))
  compileOnly(project(":exporters:otlp:all"))
  compileOnly(project(":exporters:otlp:metrics"))
  compileOnly(project(":exporters:otlp-http:trace"))
  compileOnly(project(":exporters:otlp-http:metrics"))
  compileOnly(project(":exporters:prometheus"))
  compileOnly(project(":exporters:zipkin"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(path = ":sdk:trace-shaded-deps"))

  testImplementation(project(":sdk:testing"))
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testRuntimeOnly("org.slf4j:slf4j-simple")
}

testing {
  suites {
    val testConfigError by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":extensions:trace-propagators"))
        implementation(project(":exporters:jaeger"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:otlp:metrics"))
        implementation(project(":exporters:prometheus"))
        implementation(project(":exporters:zipkin"))

        implementation("org.junit-pioneer:junit-pioneer")
      }
    }
    val testFullConfig by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":extensions:aws"))
        implementation(project(":extensions:trace-propagators"))
        implementation(project(":exporters:jaeger"))
        implementation(project(":exporters:logging"))
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:otlp:metrics"))
        implementation(project(":exporters:prometheus"))
        implementation(project(":exporters:zipkin"))
        implementation(project(":sdk-extensions:resources"))
        implementation(project(":sdk:testing"))
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
            environment("OTEL_METRICS_EXPORTER", "otlp")
            environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=test,cat=meow")
            environment("OTEL_PROPAGATORS", "tracecontext,baggage,b3,b3multi,jaeger,ottrace,xray,test")
            environment("OTEL_BSP_SCHEDULE_DELAY", "10")
            environment("OTEL_IMR_EXPORT_INTERVAL", "10")
            environment("OTEL_EXPORTER_OTLP_HEADERS", "cat=meow,dog=bark")
            environment("OTEL_EXPORTER_OTLP_TIMEOUT", "5000")
            environment("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT", "2")
            environment("OTEL_TEST_CONFIGURED", "true")
            environment("OTEL_TEST_WRAPPED", "1")
          }
        }
      }
    }
    val testInitializeRegistersGlobal by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
          }
        }
      }
    }
    val testJaeger by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:jaeger"))
        implementation(project(":exporters:jaeger-proto"))

        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
        runtimeOnly("io.grpc:grpc-netty-shaded")
      }

      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "jaeger")
            environment("OTEL_BSP_SCHEDULE_DELAY", "10")
          }
        }
      }
    }
    val testOtlpGrpc by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp:all"))
        implementation(project(":exporters:otlp:metrics"))
        implementation(project(":sdk:testing"))

        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc")
        runtimeOnly("io.grpc:grpc-netty-shaded")
      }

      targets {
        all {
          testTask {
            environment("OTEL_METRICS_EXPORTER", "otlp")
          }
        }
      }
    }
    val testOtlpHttp by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:otlp-http:trace"))
        implementation(project(":exporters:otlp-http:metrics"))
        implementation(project(":sdk:testing"))

        implementation("com.google.guava:guava")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.squareup.okhttp3:okhttp")
        implementation("com.squareup.okhttp3:okhttp-tls")
        implementation("io.opentelemetry.proto:opentelemetry-proto")
      }

      targets {
        all {
          testTask {
            environment("OTEL_METRICS_EXPORTER", "otlp")
          }
        }
      }
    }
    val testPrometheus by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:prometheus"))

        implementation("com.linecorp.armeria:armeria-junit5")
      }

      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "prometheus")
            environment("OTEL_IMR_EXPORT_INTERVAL", "10")
          }
        }
      }
    }
    val testResourceDisabledByProperty by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":sdk-extensions:resources"))
      }

      targets {
        all {
          testTask {
            jvmArgs("-Dotel.java.disabled.resource-providers=io.opentelemetry.sdk.extension.resources.OsResourceProvider,io.opentelemetry.sdk.extension.resources.ProcessResourceProvider")
            // Properties win, this is ignored.
            environment("OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS", "io.opentelemetry.sdk.extension.resources.ProcessRuntimeResourceProvider")
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
          }
        }
      }
    }
    val testResourceDisabledByEnv by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":sdk-extensions:resources"))
      }

      targets {
        all {
          testTask {
            environment("OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS", "io.opentelemetry.sdk.extension.resources.OsResourceProvider,io.opentelemetry.sdk.extension.resources.ProcessResourceProvider")
            environment("OTEL_TRACES_EXPORTER", "none")
            environment("OTEL_METRICS_EXPORTER", "none")
          }
        }
      }
    }
    val testZipkin by registering(JvmTestSuite::class) {
      dependencies {
        implementation(project(":exporters:zipkin"))

        implementation("com.linecorp.armeria:armeria-junit5")
      }

      targets {
        all {
          testTask {
            environment("OTEL_TRACES_EXPORTER", "zipkin")
            environment("OTEL_BSP_SCHEDULE_DELAY", "10")
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
