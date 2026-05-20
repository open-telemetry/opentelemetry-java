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

// OSGi test infrastructure shared across all suites.
// Each suite's source set automatically inherits these via registerOsgiSuite().
val osgiInfraImplementation: Configuration = configurations.create("osgiInfraImplementation") {
  isCanBeResolved = false
  isCanBeConsumed = false
}
val osgiInfraRuntimeOnly: Configuration = configurations.create("osgiInfraRuntimeOnly") {
  isCanBeResolved = false
  isCanBeConsumed = false
}

dependencies {
  osgiInfraImplementation("org.assertj:assertj-core")
  osgiInfraImplementation("org.junit.jupiter:junit-jupiter")
  osgiInfraImplementation("org.osgi:org.osgi.test.junit5")
  osgiInfraImplementation("org.osgi:org.osgi.test.assertj.framework")
  osgiInfraRuntimeOnly("org.junit.platform:junit-platform-launcher")
  osgiInfraRuntimeOnly("org.apache.felix:org.apache.felix.framework")
  osgiInfraRuntimeOnly("org.apache.aries.spifly:org.apache.aries.spifly.dynamic.bundle")
}

/** Typed dependency scope for an OSGi test suite, avoiding stringly-typed invoke() calls. */
class OsgiSuiteDependencies(private val sourceSet: SourceSet, private val handler: DependencyHandler) {
  fun implementation(notation: Any) = handler.add(sourceSet.implementationConfigurationName, notation)
  fun runtimeOnly(notation: Any) = handler.add(sourceSet.runtimeOnlyConfigurationName, notation)
}

/**
 * Registers tasks for an OSGi test suite (TestingBundle, GenerateBndrun, Resolve, testOSGi<Name>)
 * with a corresponding source set under src/test<Name>/java.
 *
 * @param extraRunrequires `bnd.identity` entries added to `-runrequires` to force bundles into the
 *   Felix runtime that the BND resolver won't pull in automatically.
 * @param extraRunsystempackages Package prefixes of non-OSGi classpath libraries to expose via
 *   `-runsystempackages`. Versions are resolved dynamically from the runtime classpath.
 * @param minJavaVersion If set, skips the suite when `testJavaVersion` is below this value and
 *   targets this version on the runtime classpath for dependency variant selection.
 */
fun registerOsgiSuite(
  suiteName: String,
  extraRunrequires: List<String> = emptyList(),
  extraRunsystempackages: List<String> = emptyList(),
  // SPI types that the testing bundle provides via META-INF/services (noop test implementations).
  // Generates Provide-Capability + Require-Capability registrar so SPI Fly picks them up.
  serviceLoaderProvides: List<String> = emptyList(),
  minJavaVersion: Int? = null,
  configureDependencies: OsgiSuiteDependencies.() -> Unit = {}
): TaskProvider<TestOSGi> {
  val sourceSet = sourceSets.create("test${suiteName.replaceFirstChar { it.uppercase() }}")
  OsgiSuiteDependencies(sourceSet, dependencies).configureDependencies()

  // Inherit shared OSGi test infrastructure
  configurations[sourceSet.implementationConfigurationName].extendsFrom(osgiInfraImplementation)
  configurations[sourceSet.runtimeOnlyConfigurationName].extendsFrom(osgiInfraRuntimeOnly)
  // osgi.core is compile-only (provided by the OSGi container at runtime). extendsFrom does not
  // propagate to custom source set compile classpaths, so we add it directly.
  dependencies.add(sourceSet.compileOnlyConfigurationName, "org.osgi:osgi.core")

  if (minJavaVersion != null) {
    configurations[sourceSet.runtimeClasspathConfigurationName].attributes {
      attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, minJavaVersion)
    }
  }

  val bsn = "opentelemetry-osgi-testing-$suiteName"

  val bundleTask = tasks.register<Bundle>("${suiteName}TestingBundle") {
    archiveClassifier.set("testing-$suiteName")
    from(sourceSet.output)
    bundle {
      // The Bundle task uses compileClasspath by default for BND analysis (e.g. resolving the
      // @Testable annotation to populate Test-Cases). Without this, testImplementation dependencies
      // like junit-jupiter are invisible to BND, causing Test-Cases to be empty and 0 tests to run.
      classpath(sourceSet.runtimeClasspath)
      val bndArgs = mutableListOf(
        "Bundle-SymbolicName: $bsn",
        "Test-Cases: \${classes;HIERARCHY_INDIRECTLY_ANNOTATED;org.junit.platform.commons.annotation.Testable;CONCRETE}"
      )
      if (serviceLoaderProvides.isNotEmpty()) {
        bndArgs.add("Provide-Capability: ${serviceLoaderProvides.joinToString(",") { "osgi.serviceloader;osgi.serviceloader=\"$it\"" }}")
        bndArgs.add("Require-Capability: osgi.extender;filter:=\"(osgi.extender=osgi.serviceloader.registrar)\"")
      }
      bnd(*bndArgs.toTypedArray())
    }
  }

  val runee = "JavaSE-${java.toolchain.languageVersion.get()}"
  val inputBndrun = layout.buildDirectory.file("bndrun/$suiteName.bndrun")
  val generateBndrunTask = tasks.register("${suiteName}GenerateBndrun") {
    inputs.property("bsn", bsn)
    inputs.property("runee", runee)
    inputs.property("extraRunrequires", extraRunrequires)
    outputs.file(inputBndrun)
    doLast {
      val extraEntries = extraRunrequires.joinToString("") { ",\\\n|  bnd.identity;id='$it'" }
      inputBndrun.get().asFile.apply { parentFile.mkdirs() }.writeText(
        """
        |-tester: biz.aQute.tester.junit-platform
        |-runfw: org.apache.felix.framework
        |-runee: $runee
        |
        |-runrequires: \
        |  bnd.identity;id='$bsn',\
        |  bnd.identity;id='junit-jupiter-engine',\
        |  bnd.identity;id='junit-platform-launcher'$extraEntries
        """.trimMargin()
      )
    }
  }

  val resolvedBndrun = layout.buildDirectory.file("$suiteName.bndrun")

  val resolveTask = tasks.register<Resolve>("${suiteName}Resolve") {
    dependsOn(bundleTask, generateBndrunTask)
    description = "Resolve $suiteName OSGi suite"
    group = JavaBasePlugin.VERIFICATION_GROUP
    bndrun = inputBndrun.get().asFile
    outputBndrun = resolvedBndrun
    bundles = files(sourceSet.runtimeClasspath, bundleTask.get().archiveFile)
    // The generated output embeds an absolute path to the source bndrun, making it unsafe to share
    // across machines or worktrees via the build cache.
    outputs.cacheIf { false }
    if (extraRunsystempackages.isNotEmpty()) {
      // Compute runpath/runsystempackages content here (at task registration / configuration time)
      // so the doLast closure only captures serializable types (String, RegularFileProperty).
      // Capturing SourceSet or the build script's `configurations` property in a doLast is not
      // supported by Gradle's configuration cache.
      //
      // BND identifies non-OSGi JARs by Maven group ID (e.g. OkHttp → "com.squareup.okhttp3").
      // Exception: JARs with no manifest (e.g. Kotlin stdlib) are indexed by artifact name
      // normalized to dot notation ("kotlin-stdlib" → "kotlin.stdlib") at version 0.0.0.
      // Heuristic: JetBrains artifacts lack OSGi manifests; broaden if other deps behave the same.
      fun bndCoordinate(a: org.gradle.api.artifacts.ResolvedArtifact) = if (a.moduleVersion.id.group.startsWith("org.jetbrains")) {
        "${a.name.replace('-', '.')};version=0.0.0"
      } else {
        "${a.moduleVersion.id.group};version=${a.moduleVersion.id.version}"
      }

      val resolvedArtifacts = project.configurations[sourceSet.runtimeClasspathConfigurationName]
        .resolvedConfiguration.resolvedArtifacts

      val runpathCoords = mutableListOf<String>()
      val systemPackageSpecs = mutableListOf<String>()

      for (prefix in extraRunsystempackages) {
        val normalizedPrefix = prefix.trimEnd { it.isDigit() }.lowercase()
        val matches = resolvedArtifacts.filter { it.name.lowercase().startsWith(normalizedPrefix) }
        for (artifact in matches) {
          // Export the package at the artifact's actual version; the sender bundle's optional
          // Import-Package range must include this version (managed in dependencyManagement).
          systemPackageSpecs += "$prefix;version=${artifact.moduleVersion.id.version}"
          runpathCoords += bndCoordinate(artifact)
          // Kotlin-based libraries (e.g. OkHttp) also need Kotlin stdlib on the framework classpath
          if (!artifact.moduleVersion.id.group.startsWith("org.jetbrains")) {
            resolvedArtifacts.filter { it.name.startsWith("kotlin-stdlib") }
              .forEach { runpathCoords += bndCoordinate(it) }
          }
        }
      }

      // Pre-compute the complete string to append; doLast only captures this String value.
      val appendContent = buildString {
        val distinctRunpath = runpathCoords.distinct()
        if (distinctRunpath.isNotEmpty()) append("\n-runpath: ${distinctRunpath.joinToString(", ")}")
        append("\n-runsystempackages: ${systemPackageSpecs.distinct().joinToString(", ")}\n")
      }
      inputs.property("extraRunsystempackages", extraRunsystempackages)
      inputs.property("appendContent", appendContent)

      doLast {
        resolvedBndrun.get().asFile.appendText(appendContent)
      }
    }
  }

  return tasks.register<TestOSGi>("testOSGi${suiteName.replaceFirstChar { it.uppercase() }}") {
    description = "OSGi Test $suiteName.bndrun"
    group = JavaBasePlugin.VERIFICATION_GROUP
    bndrun = resolveTask.flatMap { it.outputBndrun }
    bundles = files(sourceSet.runtimeClasspath, bundleTask.get().archiveFile)
    if (minJavaVersion != null) {
      val testJavaVersion: String? by project
      enabled = testJavaVersion == null || testJavaVersion!!.toInt() >= minJavaVersion
    }
    // BND reports success when zero tests ran (e.g. if bundles failed to start). Fail explicitly.
    val testResultsDir = layout.buildDirectory.dir("test-results/$name")
    doLast {
      check(testResultsDir.get().asFile.listFiles()?.isNotEmpty() == true) {
        "No OSGi test results found for suite '$suiteName' — bundles may have failed to start. Check the output above."
      }
    }
  }
}

// Testing the "kitchen sink" hides OSGi configuration issues. For example, opentelemetry-api has
// optional dependencies on :sdk-extensions:autoconfigure and :api:incubator. If we only test a
// bundle which includes those, then mask the fact that OSGi fails when using a bundle without those
// until opentelemetry-api OSGi configuration is updated to indicate that they are optional.

// Suite: sdk — exercises core SDK OSGi metadata in isolation
val sdkSuiteTask = registerOsgiSuite("sdk") {
  implementation(project(":sdk:all"))
}

// OTLP exporter suites. Each suite uses a specific sender implementation and documents exactly
// which system packages (if any) must be exposed for the sender to function in OSGi.

// Suite: OTLP HTTP via JDK sender — Java 11+, no non-OSGi system packages required.
// The JDK sender uses java.net.http.HttpClient which is part of the OSGi system bundle.
val otlpHttpJdkSuiteTask = registerOsgiSuite(
  "otlpHttpJdk",
  extraRunrequires = listOf("opentelemetry-exporter-sender-jdk"),
  minJavaVersion = 11,
) {
  implementation(project(":sdk:all"))
  implementation(project(":exporters:otlp:all"))
  runtimeOnly(project(":exporters:sender:jdk"))
}

// okhttp3 and okio are not OSGi bundles; they must be exposed via -runsystempackages.
val otlpHttpOkHttpSuiteTask = registerOsgiSuite(
  "otlpHttpOkHttp",
  extraRunrequires = listOf("opentelemetry-exporter-sender-okhttp"),
  extraRunsystempackages = listOf("okhttp3", "okio"),
) {
  implementation(project(":sdk:all"))
  implementation(project(":exporters:otlp:all"))
}

// okhttp3 and okio are not OSGi bundles; they must be exposed via -runsystempackages.
val otlpGrpcOkHttpSuiteTask = registerOsgiSuite(
  "otlpGrpcOkHttp",
  extraRunrequires = listOf("opentelemetry-exporter-sender-okhttp"),
  extraRunsystempackages = listOf("okhttp3", "okio"),
) {
  implementation(project(":sdk:all"))
  implementation(project(":exporters:otlp:all"))
}

// Autoconfigure suites.

// Suite: autoconfigure with OTLP + JDK sender. Exercises the full SPI loading chain across all
// SPI types.
val autoconfigureSuiteTask = registerOsgiSuite(
  "autoconfigure",
  extraRunrequires = listOf(
    "opentelemetry-exporter-sender-jdk",
    "opentelemetry-exporter-otlp",
    "opentelemetry-extension-trace-propagators",
  ),
  // Some SPIs have implementations in project modules. Others do not. To verify the ones without implementation, we provide noop implementations here.
  serviceLoaderProvides = listOf(
    "io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider",
    "io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider",
    "io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigurableMetricReaderProvider",
  ),
  minJavaVersion = 11,
) {
  implementation(project(":sdk:all"))
  implementation(project(":sdk-extensions:autoconfigure"))
  implementation(project(":exporters:otlp:all"))
  implementation(project(":extensions:trace-propagators"))
  runtimeOnly(project(":exporters:sender:jdk"))
}

// Suite: autoconfigure + declarative-config. Same as above but with declarative-config on the
// classpath, additionally exercising declarative-config bundle OSGi metadata.
val autoconfigureDeclarativeConfigSuiteTask = registerOsgiSuite(
  "autoconfigureDeclarativeConfig",
  extraRunrequires = listOf(
    "opentelemetry-exporter-sender-jdk",
    "opentelemetry-exporter-otlp",
    "opentelemetry-extension-trace-propagators",
    "opentelemetry-sdk-extension-declarative-config",
  ),
  minJavaVersion = 11,
) {
  implementation(project(":sdk:all"))
  implementation(project(":sdk-extensions:autoconfigure"))
  implementation(project(":sdk-extensions:declarative-config"))
  implementation(project(":exporters:otlp:all"))
  implementation(project(":extensions:trace-propagators"))
  runtimeOnly(project(":exporters:sender:jdk"))
}

tasks {
  jar {
    enabled = false
  }
  test {
    // We need to replace junit testing with the testOSGi tasks, so we clear test actions and add
    // dependencies on all suite tasks. As a result, running :test runs all OSGi suites.
    actions.clear()
    dependsOn(
      sdkSuiteTask,
      otlpHttpJdkSuiteTask,
      otlpHttpOkHttpSuiteTask,
      otlpGrpcOkHttpSuiteTask,
      autoconfigureSuiteTask,
      autoconfigureDeclarativeConfigSuiteTask
    )
  }
}

// Skip ossIndexAudit on test module
tasks.named("ossIndexAudit") {
  enabled = false
}
