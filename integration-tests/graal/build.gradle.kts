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
  implementation(project(path = ":sdk:trace-shaded-deps"))
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
