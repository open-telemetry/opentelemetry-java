plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Exporter Common"
otelJava.moduleName.set("io.opentelemetry.exporter.internal")

val versions: Map<String, String> by project
dependencies {
  api(project(":api:all"))
  api(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly(project(":sdk:common"))
  compileOnly(project(":exporters:common:compile-stub"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  annotationProcessor("com.google.auto.value:auto-value")

  // We include helpers shared by gRPC exporters but do not want to impose these
  // dependency on all of our consumers.
  compileOnly("com.fasterxml.jackson.core:jackson-core")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:common"))

  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.skyscreamer:jsonassert")
  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")
}

val testJavaVersion: String? by project

testing {
  suites {
    register<JvmTestSuite>("testHttpSenderProvider") {
      dependencies {
        implementation(project(":exporters:sender:jdk"))
        implementation(project(":exporters:sender:okhttp"))
      }
      targets {
        all {
          testTask {
            enabled = !testJavaVersion.equals("8")
          }
        }
      }
    }
  }
  suites {
    register<JvmTestSuite>("testGrpcSenderProvider") {
      dependencies {
        implementation(project(":exporters:sender:okhttp"))
        implementation(project(":exporters:sender:grpc-managed-channel"))

        implementation("io.grpc:grpc-stub")
        implementation("io.grpc:grpc-netty")
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}

afterEvaluate {
  tasks.named<JavaCompile>("compileTestHttpSenderProviderJava") {
    options.release.set(11)
  }
}
