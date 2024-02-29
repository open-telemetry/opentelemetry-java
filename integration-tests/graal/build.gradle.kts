plugins {
  id("otel.java-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry Graal Integration Tests"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests")

sourceSets {
  main {
    // We need to ensure that we have the shadowed classes on the classpath, without this
    // we will get the <:sdk:trace-shaded-deps> classes only, without the shadowed ones
    val traceShadedDeps = project(":sdk:trace-shaded-deps")
    output.dir(traceShadedDeps.file("build/extracted/shadow"), "builtBy" to ":sdk:trace-shaded-deps:extractShadowJar")
  }
}

dependencies {
  implementation(project(":sdk:all"))
  implementation(project(":sdk:trace-shaded-deps"))
  implementation(project(":exporters:otlp:all"))
}

// org.graalvm.buildtools.native pluging requires java 11+ as of version 0.9.26
// https://github.com/graalvm/native-build-tools/blob/master/docs/src/docs/asciidoc/index.adoc
tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.release.set(11)
  }
  withType<Test>().configureEach {
    val testJavaVersion: String? by project
    enabled = !testJavaVersion.equals("8")
  }
}

graalvmNative {
  binaries {
    named("test") {
      // Required as of junit 5.10.0: https://junit.org/junit5/docs/5.10.0/release-notes/#deprecations-and-breaking-changes
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherConfig")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.config.InstantiatingConfigurationParameterConverter")
    }
  }
  toolchainDetection.set(false)
}
