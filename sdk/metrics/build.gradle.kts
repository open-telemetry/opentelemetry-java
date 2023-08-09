import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer

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
  implementation(project(":extensions:incubator"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
  testImplementation("com.google.guava:guava")

  jmh(project(":sdk:trace"))
  jmh(project(":sdk:testing"))
  jmh(project(":sdk:metrics:testing-internal"))
  jmh("org.openjdk.jol:jol-core")
}

testing {
  suites {
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
        implementation(project(":sdk:metrics:testing-internal"))
        implementation("org.openjdk.jol:jol-core")
      }
    }
  }
}



tasks {
  named<AnimalSniffer>("animalsnifferMain") {
    // We cannot use IgnoreJreRequirement since it does not work correctly for fields.
    // https://github.com/mojohaus/animal-sniffer/issues/131
    exclude("**/concurrent/Jre*Adder*")
  }

  check {
    dependsOn(testing.suites)
  }
}
