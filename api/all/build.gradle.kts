import java.util.*

plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry API"
otelJava.moduleName.set("io.opentelemetry.api")
base.archivesName.set("opentelemetry-api")

dependencies {
  api(project(":context"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
  testImplementation("com.google.guava:guava-testlib")
}

tasks.test {
  // Configure environment variable for ConfigUtilTest
  environment("CONFIG_KEY", "environment")
}

graalvmNative {
  binaries.all {
    resources.autodetect()

    // Required as of junit 5.10.0: https://junit.org/junit5/docs/5.10.0/release-notes/#deprecations-and-breaking-changes
    buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherConfig")
    buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.config.InstantiatingConfigurationParameterConverter")

    // Required by JUnit 4
    buildArgs.add("--initialize-at-build-time=org.junit.validator.PublicClassValidator")
  }

  // org.graalvm.buildtools.native pluging requires java 11+ as of version 0.9.26
  tasks {
    withType<JavaCompile>().configureEach {
      options.release.set(11)
    }
  }
}
