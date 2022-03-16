plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry Micrometer Bridge"
otelJava.moduleName.set("io.opentelemetry.micrometer1shim")

dependencies {
  api(project(":api:all"))

  api("io.micrometer:micrometer-core")

  testImplementation(project(":sdk:metrics-testing"))
  testImplementation(project(":sdk:testing"))
}

testing {
  suites {
    // Test older LTS versions
    val testMicrometer15 by registering(JvmTestSuite::class) {
      sources {
        java {
          setSrcDirs(listOf("src/test/java"))
        }
      }
      dependencies {
        implementation(project(":sdk:metrics-testing"))
        implementation(project(":sdk:testing"))

        implementation(project.dependencies.enforcedPlatform("io.micrometer:micrometer-bom:1.5.17"))
      }
    }
    val testMicrometer16 by registering(JvmTestSuite::class) {
      sources {
        java {
          setSrcDirs(listOf("src/test/java"))
        }
      }
      dependencies {
        implementation(project(":sdk:metrics-testing"))
        implementation(project(":sdk:testing"))

        implementation(project.dependencies.enforcedPlatform("io.micrometer:micrometer-bom:1.6.13"))
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
