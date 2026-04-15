import aQute.bnd.gradle.Bundle
import aQute.bnd.gradle.Resolve
import aQute.bnd.gradle.TestOSGi

plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry OSGi Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests.osgi")

// For similar test examples see:
// https://github.com/micrometer-metrics/micrometer/tree/main/micrometer-osgi-test
// https://github.com/eclipse-osgi-technology/osgi-test/tree/main/examples/osgi-test-example-gradle

configurations.all {
  resolutionStrategy {
    // BND not compatible with JUnit 5.13+; see https://github.com/bndtools/bnd/issues/6651
    val junitVersion = "5.12.2"
    val junitLauncherVersion = "1.12.1"
    force("org.junit.jupiter:junit-jupiter:$junitVersion")
    force("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    force("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    force("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    force("org.junit.platform:junit-platform-launcher:$junitLauncherVersion")
  }
}

dependencies {
  // Testing the "kitchen sink" hides OSGi configuration issues. For example, opentelemetry-api has
  // optional dependencies on :sdk-extensions:autoconfigure and :api:incubator. If we only test a
  // bundle which includes those, then mask the fact that OSGi fails when using a bundle without those
  // until opentelemetry-api OSGi configuration is updated to indicate that they are optional.

  // TODO (jack-berg): Add additional test bundles with dependency combinations reflecting popular use cases:
  // - with OTLP exporters
  // - with autoconfigure
  // - with file configuration
  testImplementation(project(":sdk:all"))

  testImplementation("org.junit.jupiter:junit-jupiter")

  testCompileOnly("org.osgi:osgi.core")
  testImplementation("org.osgi:org.osgi.test.junit5")
  testImplementation("org.osgi:org.osgi.test.assertj.framework")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testRuntimeOnly("org.apache.felix:org.apache.felix.framework")
}

val testingBundleTask = tasks.register<Bundle>("testingBundle") {
  archiveClassifier.set("testing")
  from(sourceSets.test.get().output)
  bundle {
    // The Bundle task uses compileClasspath by default for BND analysis (e.g. resolving the
    // @Testable annotation to populate Test-Cases). Without this, testImplementation dependencies
    // like junit-jupiter are invisible to BND, causing Test-Cases to be empty and 0 tests to run.
    classpath(sourceSets.test.get().runtimeClasspath)
    bnd(
      "Test-Cases: \${classes;HIERARCHY_INDIRECTLY_ANNOTATED;org.junit.platform.commons.annotation.Testable;CONCRETE}",
      "Import-Package: javax.annotation.*;resolution:=optional;version=\"\${@}\",*"
    )
  }
}

val resolveTask = tasks.register<Resolve>("resolve") {
  dependsOn(testingBundleTask)
  project.ext.set("osgiRunee", "JavaSE-${java.toolchain.languageVersion.get()}")
  description = "Resolve test.bndrun"
  group = JavaBasePlugin.VERIFICATION_GROUP
  bndrun = file("test.bndrun")
  outputBndrun = layout.buildDirectory.file("test.bndrun")
  bundles = files(sourceSets.test.get().runtimeClasspath, testingBundleTask.get().archiveFile)
  // The generated output embeds an absolute path to the source bndrun, making it unsafe to share
  // across machines or worktrees via the build cache.
  outputs.cacheIf { false }
}

val testOSGiTask = tasks.register<TestOSGi>("testOSGi") {
  description = "OSGi Test test.bndrun"
  group = JavaBasePlugin.VERIFICATION_GROUP
  bndrun = resolveTask.flatMap { it.outputBndrun }
  bundles = files(sourceSets.test.get().runtimeClasspath, testingBundleTask.get().archiveFile)
  // BND reports success when zero tests ran (e.g. if bundles failed to start). Fail explicitly.
  val testResultsDir = layout.buildDirectory.dir("test-results/testOSGi")
  doLast {
    check(testResultsDir.get().asFile.listFiles()?.isNotEmpty() == true) {
      "No OSGi test results found — bundles may have failed to start. Check the output above."
    }
  }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
  dependsOn(testOSGiTask)
}

tasks {
  jar {
    enabled = false
  }
  test {
    // We need to replace junit testing with the testOSGi task, so we clear test actions and add a dependency on testOSGi.
    // As a result, running :test runs only :testOSGi.
    actions.clear()
    dependsOn(testOSGiTask)
  }
}

// Skip ossIndexAudit on test module
tasks.named("ossIndexAudit") {
  enabled = false
}
