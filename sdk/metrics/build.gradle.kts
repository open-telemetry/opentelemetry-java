plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK Metrics"
otelJava.moduleName.set("io.opentelemetry.sdk.metrics")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))
  compileOnly(project(":api:incubator"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation("com.google.guava:guava")
  testImplementation("com.google.guava:guava-testlib")

  jmh(project(":sdk:trace"))
  jmh(project(":sdk:testing"))
}

dependencyCheck {
  skipConfigurations.add("debugEnabledTestAnnotationProcessor")
}

testing {
  suites {
    register<JvmTestSuite>("testIncubating") {
      dependencies {
        implementation(project(":sdk:testing"))
        implementation(project(":api:incubator"))
        implementation("com.google.guava:guava")
      }
    }
    register<JvmTestSuite>("debugEnabledTest") {
      targets {
        all {
          testTask.configure {
            jvmArgs("-Dotel.experimental.sdk.metrics.debug=true")
          }
        }
      }
    }
    register<JvmTestSuite>("jmhBasedTest") {
      dependencies {
        implementation("org.openjdk.jmh:jmh-core")
        implementation("org.openjdk.jmh:jmh-generator-bytecode")
        annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
