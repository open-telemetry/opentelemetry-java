import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry Graal Integration Tests"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_17)

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

// org.graalvm.buildtools.native plugin requires java 17+ as of version 0.11.0
// https://github.com/graalvm/native-build-tools/blob/master/docs/src/docs/asciidoc/index.adoc

graalvmNative {
  binaries {
    named("test") {
      // JUnit initialization is (mostly) handled automatically by the GraalVM plugin 0.11+
      // Required as of junit 5.14.1
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.discovery.MethodSegmentResolver")
    }
  }
  toolchainDetection.set(false)
}

// GraalVM Native Build Tools plugin is not yet compatible with configuration cache
// https://github.com/graalvm/native-build-tools/issues/477
tasks.configureEach {
  if (name.startsWith("native")) {
    notCompatibleWithConfigurationCache("GraalVM Native Build Tools plugin is not compatible with configuration cache")
  }
}
