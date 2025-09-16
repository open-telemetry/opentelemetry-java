plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}
apply<io.opentelemetry.gradle.OtelVersionClassPlugin>()

description = "OpenTelemetry Protocol (OTLP) Exporters"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp")
base.archivesName.set("opentelemetry-exporter-otlp")

dependencies {
  api(project(":sdk:trace"))
  api(project(":sdk:metrics"))
  api(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))
  implementation(project(":exporters:sender:okhttp"))
  implementation(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly(project(":api:incubator"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":exporters:otlp:testing-internal"))
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc-protocol")
  testImplementation("io.grpc:grpc-stub")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation(project(":exporters:sender:grpc-managed-channel"))
  jmhImplementation("com.linecorp.armeria:armeria")
  jmhImplementation("com.linecorp.armeria:armeria-grpc")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("com.squareup.okhttp3:okhttp-jvm")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

val testJavaVersion: String? by project

testing {
  suites {
    listOf(
      "LATEST",
      "4.11.0"
    ).forEach {
      register<JvmTestSuite>("testOkHttpVersion$it") {
        sources {
          java {
            setSrcDirs(listOf("src/testDefaultSender/java"))
          }
        }
        dependencies {
          implementation(project(":exporters:sender:okhttp"))
          implementation(project(":exporters:otlp:testing-internal"))

          implementation(platform("com.squareup.okhttp3:okhttp-bom")) {
            // Only impose dependency constraint if not testing the LATEST version, which is defined in /dependencyManagement/build.gradle.kts
            if (!it.equals("LATEST")) {
              version {
                strictly(it)
              }
            }
          }
          if (it.equals("LATEST")) {
            implementation("com.squareup.okhttp3:okhttp-jvm")
          } else {
            implementation("com.squareup.okhttp3:okhttp")
          }

          implementation("io.grpc:grpc-stub")
        }

        targets {
          all {
            testTask {
              // Only enable test suite for non-LATEST in GitHub CI (CI=true)
              enabled = it.equals("LATEST") || "true".equals(System.getenv("CI"))
              systemProperty("expected.okhttp.version", it)
            }
          }
        }
      }
    }

    register<JvmTestSuite>("testGrpcNetty") {
      dependencies {
        implementation(project(":exporters:sender:grpc-managed-channel"))
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-netty")
        implementation("io.grpc:grpc-stub")
        implementation("io.grpc:grpc-inprocess")
      }
      targets {
        all {
          testTask {
            systemProperty(
              "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
              "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider"
            )
          }
        }
      }
    }
    register<JvmTestSuite>("testGrpcNettyShaded") {
      dependencies {
        implementation(project(":exporters:sender:grpc-managed-channel"))
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-netty-shaded")
        implementation("io.grpc:grpc-stub")
        implementation("io.grpc:grpc-inprocess")
      }
      targets {
        all {
          testTask {
            systemProperty(
              "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
              "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider"
            )
          }
        }
      }
    }
    register<JvmTestSuite>("testGrpcOkhttp") {
      dependencies {
        implementation(project(":exporters:sender:grpc-managed-channel"))
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-okhttp")
        implementation("io.grpc:grpc-stub")
        implementation("io.grpc:grpc-inprocess")
      }
      targets {
        all {
          testTask {
            systemProperty(
              "io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider",
              "io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSenderProvider"
            )
          }
        }
      }
    }
    register<JvmTestSuite>("testJdkHttpSender") {
      dependencies {
        implementation(project(":exporters:sender:jdk"))
        implementation(project(":exporters:otlp:testing-internal"))

        implementation("io.grpc:grpc-stub")
      }
      targets {
        all {
          testTask {
            systemProperty(
              "io.opentelemetry.exporter.internal.http.HttpSenderProvider",
              "io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderProvider"
            )
            enabled = !testJavaVersion.equals("8")
          }
        }
      }
    }
    register<JvmTestSuite>("testSpanPipeline") {
      dependencies {
        implementation("io.opentelemetry.proto:opentelemetry-proto")
        implementation("com.linecorp.armeria:armeria-grpc-protocol")
        implementation("com.linecorp.armeria:armeria-junit5")
      }
    }
  }
}

tasks {
  check {
    dependsOn(
      testing.suites.filter {
        // Mainly to conveniently profile through IDEA. Don't add to check task, it's for
        // manual invocation.
        name != "testSpanPipeline"
      },
    )
  }
}

afterEvaluate {
  tasks.named<JavaCompile>("compileTestJdkHttpSenderJava") {
    options.release.set(11)
  }
}
