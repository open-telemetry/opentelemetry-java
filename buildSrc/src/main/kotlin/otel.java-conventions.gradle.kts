import io.opentelemetry.gradle.OtelJavaExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  `java-library`

  checkstyle
  eclipse
  idea

  id("com.diffplug.spotless")

  id("otel.errorprone-conventions")
  id("otel.jacoco-conventions")
}

val otelJava = extensions.create<OtelJavaExtension>("otelJava")

base {
  // May be set already by a parent project, only set if not.
  // TODO(anuraaga): Make this less hacky by creating a "module group" plugin.
  if (!archivesName.get().startsWith("opentelemetry-")) {
    archivesName.set("opentelemetry-${name}")
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }

  withJavadocJar()
  withSourcesJar()
}

checkstyle {
  configDirectory.set(file("$rootDir/buildscripts/"))
  toolVersion = "8.12"
  isIgnoreFailures = false
  configProperties["rootDir"] = rootDir
}

val testJavaVersion = gradle.startParameter.projectProperties.get("testJavaVersion")?.let(JavaVersion::toVersion)

tasks {
  withType<JavaCompile>().configureEach {
    with(options) {
      release.set(8)

      if (name != "jmhCompileGeneratedClasses") {
        compilerArgs.addAll(listOf(
          "-Xlint:all",
          // We suppress the "try" warning because it disallows managing an auto-closeable with
          // try-with-resources without referencing the auto-closeable within the try block.
          "-Xlint:-try",
          // We suppress the "processing" warning as suggested in
          // https://groups.google.com/forum/#!topic/bazel-discuss/_R3A9TJSoPM
          "-Xlint:-processing",
          // We suppress the "options" warning because it prevents compilation on modern JDKs
          "-Xlint:-options",

          // Fail build on any warning
          "-Werror"
        ))
      }

      //disable deprecation warnings for the protobuf module
      if (project.name == "proto") {
        compilerArgs.add("-Xlint:-deprecation")
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

    if (testJavaVersion != null) {
      javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(testJavaVersion.majorVersion))
      })
    }

    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
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

      links("https://docs.oracle.com/javase/8/docs/api/")
      addBooleanOption("Xdoclint:all,-missing", true)
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
        "Implementation-Version" to project.version)
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

  register("generateVersionResource") {
    val moduleName = otelJava.moduleName
    val propertiesDir = moduleName.map { file("build/generated/properties/${it.replace('.', '/')}") }

    inputs.property("project.version", project.version.toString())
    outputs.dir(propertiesDir)

    doLast {
      File(propertiesDir.get(), "version.properties").writeText("sdk.version=${project.version}")
    }
  }
}

sourceSets {
  main {
    output.dir("build/generated/properties", "builtBy" to "generateVersionResource")
  }
}

configurations.configureEach {
  resolutionStrategy {
    failOnVersionConflict()
    preferProjectModules()
  }
}

spotless {
  java {
    googleJavaFormat("1.9")
    licenseHeaderFile(rootProject.file("buildscripts/spotless.license.java"), "(package|import|class|// Includes work from:)")
  }
}

val dependencyManagement by configurations.creating {
  isCanBeConsumed = false
  isCanBeResolved = false
  isVisible = false
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

  compileOnly("com.google.auto.value:auto-value-annotations")
  compileOnly("com.google.code.findbugs:jsr305")

  testCompileOnly("com.google.auto.value:auto-value-annotations")
  testCompileOnly("com.google.errorprone:error_prone_annotations")
  testCompileOnly("com.google.code.findbugs:jsr305")

  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
  testImplementation("org.assertj:assertj-core")
  testImplementation("org.awaitility:awaitility")
  testImplementation("io.github.netmikey.logunit:logunit-jul")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

  errorprone("com.google.errorprone:error_prone_core")
  errorprone("com.uber.nullaway:nullaway")

  annotationProcessor("com.google.guava:guava-beta-checker")

  // Workaround for @javax.annotation.Generated
  // see: https://github.com/grpc/grpc-java/issues/3633
  compileOnly("javax.annotation:javax.annotation-api")
}
