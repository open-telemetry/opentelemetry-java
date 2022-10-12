plugins {
  id("otel.java-conventions")
  id("java-library")
  id("org.graalvm.buildtools.native") version "0.9.14"
}

description = "OpenTelemetry Graal Integration Tests"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests")

sourceSets {
  main {
    val traceShadedDeps = project(":sdk:trace-shaded-deps")
    output.dir(traceShadedDeps.file("build/extracted/shadow"), "builtBy" to ":sdk:trace-shaded-deps:extractShadowJar")
  }
}

dependencies {
  implementation(project(path = ":sdk:trace-shaded-deps"))

  testImplementation("org.junit.jupiter:junit-jupiter")
}

graalvmNative {
  binaries {
    named("main") {
      verbose.set(true)
    }
  }
}

tasks.withType<org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask>().configureEach {
  disableToolchainDetection.set(true)
}

tasks.test {
  useJUnitPlatform()
}
