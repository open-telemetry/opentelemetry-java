plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Log SDK"
otelJava.moduleName.set("io.opentelemetry.sdk.logs")

sourceSets {
  create("jmhJava9") {
    java {
      srcDirs("src/jmh/java9")
    }
    compileClasspath += sourceSets.jmh.get().compileClasspath
    compileClasspath += sourceSets.jmh.get().output
  }
}

tasks {
  named<JavaCompile>("compileJmhJava9Java") {
    options.release = 9
    dependsOn("compileJmhJava")
  }

  // Configure JMH jar as multi-release jar
  named<Jar>("jmhJar") {
    into("META-INF/versions/9") {
      from(sourceSets["jmhJava9"].output)
    }
    manifest.attributes(
      "Multi-Release" to "true"
    )
  }
}

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))
  compileOnly(project(":api:incubator"))

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
        implementation("io.opentelemetry.semconv:opentelemetry-semconv-incubating")
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
