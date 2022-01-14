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

  testImplementation("org.awaitility:awaitility")
  testImplementation("com.google.guava:guava")
  testImplementation("org.junit-pioneer:junit-pioneer")
}

testing {
  suites {
    val grpcInOtelTest by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.grpc:grpc-context")
      }
    }

    val otelInGrpcTest by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.grpc:grpc-context")
      }
    }

    val braveInOtelTest by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    val otelInBraveTest by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    val otelAsBraveTest by registering(JvmTestSuite::class) {
      dependencies {
        implementation("io.zipkin.brave:brave")
      }
    }

    val storageWrappersTest by registering(JvmTestSuite::class) {
    }

    val strictContextEnabledTest by registering(JvmTestSuite::class) {
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
