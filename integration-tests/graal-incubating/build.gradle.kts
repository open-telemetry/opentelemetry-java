plugins {
  id("otel.java-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry Graal Integration Tests (Incubating)"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests.incubating")

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
  implementation(project(":sdk:testing"))
  implementation(project(":exporters:otlp:all"))
  implementation(project(":api:incubator"))
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
      // Required as of junit 5.13.0: https://junit.org/junit5/docs/5.13.0/release-notes/#deprecations-and-breaking-changes
      val initializeAtBuildTime = listOf(
        "org.junit.jupiter.api.DisplayNameGenerator\$IndicativeSentences",
        "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$ClassInfo",
        "org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$LifecycleMethods",
        "org.junit.jupiter.engine.descriptor.ClassTemplateInvocationTestDescriptor",
        "org.junit.jupiter.engine.descriptor.ClassTemplateTestDescriptor",
        "org.junit.jupiter.engine.descriptor.DynamicDescendantFilter\$Mode",
        "org.junit.jupiter.engine.descriptor.ExclusiveResourceCollector\$1",
        "org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor\$MethodInfo",
        "org.junit.jupiter.engine.discovery.ClassSelectorResolver\$DummyClassTemplateInvocationContext",
        "org.junit.platform.engine.support.store.NamespacedHierarchicalStore\$EvaluatedValue",
        "org.junit.platform.launcher.core.DiscoveryIssueNotifier",
        "org.junit.platform.launcher.core.HierarchicalOutputDirectoryProvider",
        "org.junit.platform.launcher.core.LauncherDiscoveryResult\$EngineResultInfo",
        "org.junit.platform.launcher.core.LauncherPhase",
        "org.junit.platform.suite.engine.DiscoverySelectorResolver",
        "org.junit.platform.suite.engine.SuiteTestDescriptor\$DiscoveryIssueForwardingListener",
        "org.junit.platform.suite.engine.SuiteTestDescriptor\$LifecycleMethods",
      )
      buildArgs.add("--initialize-at-build-time=${initializeAtBuildTime.joinToString(",")}")
    }
  }
  toolchainDetection.set(false)
}
