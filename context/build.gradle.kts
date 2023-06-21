plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Context (Incubator)"
otelJava.moduleName.set("io.opentelemetry.context")

dependencies {
  // MustBeClosed
  compileOnly("com.google.errorprone:error_prone_annotations")

  testImplementation("com.google.guava:guava")
}

testing {
  suites {
    register<JvmTestSuite>("grpcInOtelTest") {
      dependencies {
        implementation("io.grpc:grpc-context")
      }
    }

    register<JvmTestSuite>("otelInGrpcTest") {
      dependencies {
        implementation("io.grpc:grpc-context")
      }
    }

    register<JvmTestSuite>("braveInOtelTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("otelInBraveTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("otelAsBraveTest") {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    register<JvmTestSuite>("storageWrappersTest") {
    }

    register<JvmTestSuite>("strictContextEnabledTest") {
      dependencies {
        implementation(project(":api:all"))
      }

      targets {
        all {
          testTask.configure {
            jvmArgs("-Dio.opentelemetry.context.enableStrictContext=true")
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
