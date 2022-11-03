plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

val javaagent by configurations.creating

description = "OpenTelemetry OpenCensus Shim"
otelJava.moduleName.set("io.opentelemetry.opencensusshim")

dependencies {
  api(project(":api:all"))
  api(project(":extensions:trace-propagators"))
  api(project(":sdk:all"))
  api(project(":sdk:metrics"))

  api("io.opencensus:opencensus-api")
  api("io.opencensus:opencensus-impl-core")
  api("io.opencensus:opencensus-exporter-metrics-util")

  testImplementation(project(":sdk:all"))
  testImplementation(project(":sdk:testing"))

  testImplementation("io.opencensus:opencensus-impl")
  testImplementation("io.opencensus:opencensus-contrib-exemplar-util")

  javaagent("io.opentelemetry.javaagent:opentelemetry-javaagent:1.19.1")
}

tasks.named<Test>("test") {
  // We must force a fork per-test class because OpenCensus pollutes globals with no restorative
  // methods available.
  setForkEvery(1)
  maxParallelForks = 3
}

testing {
  suites {
    val integrationTest by registering(JvmTestSuite::class) {
      dependencies {
      }

      targets {
        all {
          testTask.configure {
            jvmArgs("-javaagent:${javaagent.asPath}")
            environment("OTEL_TRACES_EXPORTER", "logging")
          }
        }
      }
    }
  }
}
