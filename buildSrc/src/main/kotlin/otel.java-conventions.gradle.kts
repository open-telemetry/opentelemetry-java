import io.opentelemetry.gradle.OtelJavaExtension
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  `java-library`

  checkstyle
  eclipse
  idea

  id("biz.aQute.bnd.builder")
  id("otel.errorprone-conventions")
  id("otel.jacoco-conventions")
  id("otel.spotless-conventions")
  id("org.owasp.dependencycheck")
}

val otelJava = extensions.create<OtelJavaExtension>("otelJava")

base {
  // May be set already by a parent project, only set if not.
  // TODO(anuraaga): Make this less hacky by creating a "module group" plugin.
  if (!archivesName.get().startsWith("opentelemetry-")) {
    archivesName.set("opentelemetry-$name")
  }
}

// normalize timestamps and file ordering in jars, making the outputs reproducible
// see open-telemetry/opentelemetry-java#4488
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }

  withJavadocJar()
  withSourcesJar()
}

checkstyle {
  configDirectory.set(file("$rootDir/buildscripts/"))
  toolVersion = "13.0.0"
  isIgnoreFailures = false
  configProperties["rootDir"] = rootDir
}

dependencyCheck {
  skipConfigurations = mutableListOf(
    "errorprone",
    "checkstyle",
    "annotationProcessor",
    "java9AnnotationProcessor",
    "moduleAnnotationProcessor",
    "testAnnotationProcessor",
    "testJpmsAnnotationProcessor",
    "animalsniffer",
    "spotless996155815", // spotless996155815 is a weird configuration that's only added in jaeger-proto, jaeger-remote-sampler
    "js2p",
    "jmhAnnotationProcessor",
    "jmhBasedTestAnnotationProcessor",
    "jmhCompileClasspath",
    "jmhRuntimeClasspath",
    "jmhRuntimeOnly")
  failBuildOnCVSS = 7.0f // fail on high or critical CVE
  analyzers.assemblyEnabled = false // not sure why its trying to analyze .NET assemblies
  nvd.apiKey = System.getenv("NVD_API_KEY")
}

val testJavaVersion = gradle.startParameter.projectProperties.get("testJavaVersion")?.let(JavaVersion::toVersion)

tasks {
  withType<JavaCompile>().configureEach {
    with(options) {
      release.set(otelJava.minJavaVersionSupported.map { it.majorVersion.toInt() })

      if (name != "jmhCompileGeneratedClasses") {
        compilerArgs.addAll(
          listOf(
            "-Xlint:all",
            // We suppress the "try" warning because it disallows managing an auto-closeable with
            // try-with-resources without referencing the auto-closeable within the try block.
            "-Xlint:-try",
            // We suppress the "processing" warning as suggested in
            // https://groups.google.com/forum/#!topic/bazel-discuss/_R3A9TJSoPM
            "-Xlint:-processing",
            // We suppress the "options" warning because it prevents compilation on modern JDKs
            "-Xlint:-options",
            "-Xlint:-serial",
            "-Xlint:-this-escape",
            // Fail build on any warning
            "-Werror",
          ),
        )
      }

      encoding = "UTF-8"

      if (name.contains("Test")) {
        // serialVersionUID is basically guaranteed to be useless in tests
        compilerArgs.add("-Xlint:-serial")
      }
    }
  }

  withType<Test>().configureEach {
    useJUnitPlatform()

    val defaultMaxRetries = if (System.getenv().containsKey("CI")) 2 else 0
    val maxTestRetries = gradle.startParameter.projectProperties["maxTestRetries"]?.toInt() ?: defaultMaxRetries

    develocity.testRetry {
      // You can see tests that were retried by this mechanism in the collected test reports and build scans.
      maxRetries.set(maxTestRetries);
    }

    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true

      showStandardStreams = true
    }
    maxHeapSize = "1500m"
  }

  withType<Javadoc>().configureEach {
    exclude("io/opentelemetry/**/internal/**")

    with(options as StandardJavadocDocletOptions) {
      source = "8"
      encoding = "UTF-8"
      docEncoding = "UTF-8"
      breakIterator(true)

      addBooleanOption("html5", true)
      addBooleanOption("Xdoclint:all,-missing", true)
    }
  }

  named<Jar>("jar") {
    // Configure OSGi metadata
    bundle {
      bnd(mapOf(
        // Once https://github.com/open-telemetry/opentelemetry-java/issues/6970 is resolved, exclude .internal packages
        "-exportcontents" to "io.opentelemetry.*",
        "Import-Package" to "javax.annotation.*;resolution:=optional;version=\"\${@}\",*",
      ))
    }
  }

  withType<Jar>().configureEach {
    inputs.property("moduleName", otelJava.moduleName)

    manifest {
      attributes(
        "Automatic-Module-Name" to otelJava.moduleName,
        "Built-By" to System.getProperty("user.name"),
        "Built-JDK" to System.getProperty("java.version"),
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
      )
    }
  }

  afterEvaluate {
    withType<Javadoc>().configureEach {
      with(options as StandardJavadocDocletOptions) {
        val title = "${project.description}"
        docTitle = title
        windowTitle = title
      }
    }
  }
}

afterEvaluate {
  tasks.withType<Test>().configureEach {
    if (testJavaVersion != null) {
      javaLauncher.set(
        javaToolchains.launcherFor {
          languageVersion.set(JavaLanguageVersion.of(testJavaVersion.majorVersion))
        }
      )
      isEnabled = isEnabled && testJavaVersion >= otelJava.minJavaVersionSupported.get()
    }
  }
}

// Add version information to published artifacts.
plugins.withId("otel.publish-conventions") {
  tasks {
    register("generateVersionResource") {
      val moduleName = otelJava.moduleName
      val propertiesDir = moduleName.map { File(layout.buildDirectory.asFile.get(), "generated/properties/${it.replace('.', '/')}") }
      val versionProperty = project.version.toString()

      inputs.property("project.version", versionProperty)
      outputs.dir(propertiesDir)

      doLast {
        File(propertiesDir.get(), "version.properties").writeText("sdk.version=${versionProperty}")
      }
    }
  }

  sourceSets {
    main {
      output.dir("${layout.buildDirectory.asFile.get()}/generated/properties", "builtBy" to "generateVersionResource")
    }
  }
}

configurations.configureEach {
  resolutionStrategy {
    failOnVersionConflict()
    preferProjectModules()
  }
}

val dependencyManagement by configurations.creating {
  isCanBeConsumed = false
  isCanBeResolved = false
}

val mockitoAgent by configurations.creating {
  extendsFrom(dependencyManagement)
}

dependencies {
  dependencyManagement(platform(project(":dependencyManagement")))
  afterEvaluate {
    configurations.configureEach {
      if (isCanBeResolved && !isCanBeConsumed) {
        extendsFrom(dependencyManagement)
      }
    }
  }

  mockitoAgent("org.mockito:mockito-core")

  compileOnly("com.google.auto.value:auto-value-annotations")
  compileOnly("com.google.code.findbugs:jsr305")

  annotationProcessor("com.google.guava:guava-beta-checker")

  // Workaround for @javax.annotation.Generated
  // see: https://github.com/grpc/grpc-java/issues/3633
  compileOnly("javax.annotation:javax.annotation-api")

  modules {
    // checkstyle uses the very old google-collections which causes Java 9 module conflict with
    // guava which is also on the classpath
    module("com.google.collections:google-collections") {
      replacedBy("com.google.guava:guava", "google-collections is now part of Guava")
    }
  }
}

testing {
  suites.withType(JvmTestSuite::class).configureEach {
    useJUnitJupiter()

    dependencies {
      implementation(project(project.path))

      implementation(project(":testing-internal"))

      compileOnly("com.google.auto.value:auto-value-annotations")
      compileOnly("com.google.errorprone:error_prone_annotations")
      compileOnly("com.google.code.findbugs:jsr305")

      implementation("nl.jqno.equalsverifier:equalsverifier")
      implementation("org.mockito:mockito-core")
      implementation("org.mockito:mockito-junit-jupiter")
      implementation("org.assertj:assertj-core")
      implementation("org.awaitility:awaitility")
      implementation("org.junit-pioneer:junit-pioneer")
      implementation("io.github.netmikey.logunit:logunit-jul")

      runtimeOnly("org.slf4j:slf4j-simple")
    }

    targets {
      all {
        testTask.configure {
          systemProperty("java.util.logging.config.class", "io.opentelemetry.internal.testing.slf4j.JulBridgeInitializer")

          // Starting in java 21, dynamically attaching agents triggers warnings. Mockito depends on
          // agents to redefine classes. Hence, on java 21+ we get warnings of the form:
          //    WARNING: A Java agent has been loaded dynamically (/Users/jberg/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.12.19/450917cf3b358b691a824acf4c67aa89c826f67e/byte-buddy-agent-1.12.19.jar)
          //    WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
          //    WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
          //    WARNING: Dynamic loading of agents will be disallowed by default in a future release
          // To remove these warnings, we attach the byte-buddy-agent used by mockito directly.
          val mockitoAgent: FileCollection = mockitoAgent
          doFirst {
            val mockitoAgentJar = mockitoAgent.files.single { it.name.contains("byte-buddy-agent")}
            jvmArgs("-javaagent:${mockitoAgentJar}")
          }
        }
      }
    }
  }
}
