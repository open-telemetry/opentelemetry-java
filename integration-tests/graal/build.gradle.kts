import org.gradle.api.JavaVersion

plugins {
  id("otel.java-conventions")
  id("org.graalvm.buildtools.native")
}

description = "OpenTelemetry Graal Integration Tests"
otelJava.moduleName.set("io.opentelemetry.graal.integration.tests")
otelJava.minJavaVersionSupported.set(JavaVersion.VERSION_11)

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

// org.graalvm.buildtools.native plugin requires java 11+ as of version 0.9.26
// https://github.com/graalvm/native-build-tools/blob/master/docs/src/docs/asciidoc/index.adoc

graalvmNative {
  binaries {
    named("test") {
      // Required as of junit 5.10.0: https://junit.org/junit5/docs/5.10.0/release-notes/#deprecations-and-breaking-changes
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherConfig")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.config.InstantiatingConfigurationParameterConverter")
      // Required as of junit 5.13.0: https://junit.org/junit5/docs/5.13.0/release-notes/#deprecations-and-breaking-changes
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.api.DisplayNameGenerator\$IndicativeSentences")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$ClassInfo")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$LifecycleMethods")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.ClassTemplateInvocationTestDescriptor")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.ClassTemplateTestDescriptor")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.DynamicDescendantFilter\$Mode")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.ExclusiveResourceCollector\$1")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor\$MethodInfo")
      buildArgs.add("--initialize-at-build-time=org.junit.jupiter.engine.discovery.ClassSelectorResolver\$DummyClassTemplateInvocationContext")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.engine.support.store.NamespacedHierarchicalStore\$EvaluatedValue")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.DiscoveryIssueNotifier")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.HierarchicalOutputDirectoryProvider")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherDiscoveryResult\$EngineResultInfo")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherPhase")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.suite.engine.DiscoverySelectorResolver")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.suite.engine.SuiteTestDescriptor\$DiscoveryIssueForwardingListener")
      buildArgs.add("--initialize-at-build-time=org.junit.platform.suite.engine.SuiteTestDescriptor\$LifecycleMethods")
    }
  }
  toolchainDetection.set(false)
}
