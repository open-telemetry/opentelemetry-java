plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Prometheus Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.prometheus")

dependencies {
  api(project(":sdk:metrics"))

  implementation(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly("com.sun.net.httpserver:http")

  testImplementation(project(":semconv"))

  testImplementation("io.opentelemetry.proto:opentelemetry-proto")

  testImplementation("com.google.guava:guava")
  testImplementation("com.linecorp.armeria:armeria")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc-protocol")
  testImplementation("com.fasterxml.jackson.jr:jackson-jr-stree")
  testImplementation("org.testcontainers:junit-jupiter")
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}

// TODO(anuraaga): Move to conventions.

testing {
  suites {
    val testJpms by registering(JvmTestSuite::class) {
      targets {
        all {
          testTask.configure {
            enabled = gradle.startParameter.projectProperties.get("testJavaVersion") != "8"
          }
        }
      }
    }
  }
}

sourceSets {
  val module by creating
  main {
    output.dir(mapOf("builtBy" to "compileModuleJava"), module.java.destinationDirectory)
  }
}

configurations {
  named("moduleImplementation") {
    extendsFrom(configurations["implementation"])
  }
}

tasks {
  jar {
    manifest.attributes.remove("Automatic-Module-Name")

    exclude("**/HackForJpms.class")
  }

  compileJava {
    exclude("module-info.java")
  }

  withType<Checkstyle>().configureEach {
    exclude("module-info.java")
  }

  val compileModuleJava by existing(JavaCompile::class) {
    with(options) {
      release.set(9)
    }
  }

  named<JavaCompile>("compileTestJpmsJava") {
    with(options) {
      release.set(9)
      compilerArgs.add("--add-modules=org.junit.jupiter.api")
      compilerArgs.add("--add-reads=io.opentelemetry.exporters.prometheus.test=org.junit.jupiter.api")
    }
  }
}
