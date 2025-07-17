plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Context (Incubator)"
otelJava.moduleName.set("io.opentelemetry.context")

dependencies {
  api(project(":common"))
  // MustBeClosed
  compileOnly("com.google.errorprone:error_prone_annotations")

  testImplementation("com.google.guava:guava")
}

dependencyCheck {
  skipConfigurations.add("braveInOtelTestAnnotationProcessor")
  skipConfigurations.add("grpcInOtelTestAnnotationProcessor")
  skipConfigurations.add("otelAsBraveTestAnnotationProcessor")
  skipConfigurations.add("otelInBraveTestAnnotationProcessor")
  skipConfigurations.add("otelInGrpcTestAnnotationProcessor")
  skipConfigurations.add("storageWrappersTestAnnotationProcessor")
  skipConfigurations.add("strictContextEnabledTestAnnotationProcessor")
}

testing {
  suites {
    register<JvmTestSuite>("grpcInOtelTest") {
      dependencies {
        implementation("io.grpc:grpc-api")
      }
    }

    register<JvmTestSuite>("otelInGrpcTest") {
      dependencies {
        implementation("io.grpc:grpc-api")
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
