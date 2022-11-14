plugins {
  id("otel.java-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry Graal Integration Tests"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests")

sourceSets {
  main {
    // We need to ensure that we have the shadowed classes on the classpath, without this
    // we will get the <:sdk:common-shaded-deps> classes only, without the shadowed ones
    val traceShadedDeps = project(":sdk:common-shaded-deps")
    output.dir(traceShadedDeps.file("build/extracted/shadow"), "builtBy" to ":sdk:common-shaded-deps:extractShadowJar")
  }
}

dependencies {
  implementation(project(path = ":sdk:common-shaded-deps"))
}

graalvmNative {
  binaries {
    named("main") {
      verbose.set(true)
    }
  }
  toolchainDetection.set(false)
}
