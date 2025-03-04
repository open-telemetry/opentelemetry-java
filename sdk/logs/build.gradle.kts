plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Log SDK"
otelJava.moduleName.set("io.opentelemetry.sdk.logs")

sourceSets {
  create("incubating")
}

java {
  registerFeature("incubating") {
    usingSourceSet(sourceSets["incubating"])
  }
}

val incubatingImplementation by configurations.existing

val compileOnly by configurations.existing {
  extendsFrom(incubatingImplementation.get())
}

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))

  incubatingImplementation(project(":api:incubator"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))

  testImplementation("org.awaitility:awaitility")
  testImplementation("com.google.guava:guava")
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
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}
