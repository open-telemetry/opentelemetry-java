plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry All"
otelJava.moduleName.set("io.opentelemetry.all")

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}

val testTasks = mutableListOf<Task>()
val jarTasks = mutableListOf<Jar>()

dependencies {
  rootProject.subprojects.forEach { subproject ->
    // Generate aggregate coverage report for published modules that enable jacoco.
    subproject.plugins.withId("jacoco") {
      subproject.plugins.withId("maven-publish") {
        implementation(project(subproject.path)) {
          isTransitive = false
        }
        subproject.tasks.withType<Test>().configureEach {
          testTasks.add(this)
        }
        subproject.tasks.withType<Jar>().forEach {
          if (it.archiveClassifier.get().isEmpty() && !it.name.contains("jmh")) {
            jarTasks.add(it)
          }
        }
      }
    }
  }

  testImplementation("com.tngtech.archunit:archunit-junit5")
}

// Custom task type for writing artifacts and jars - configuration cache compatible
abstract class WriteArtifactsAndJars : DefaultTask() {
  @get:Input
  abstract val artifactData: MapProperty<String, String>

  @get:OutputFile
  abstract val outputFile: RegularFileProperty

  @TaskAction
  fun writeFile() {
    val file = outputFile.get().asFile
    file.parentFile.mkdirs()
    val content = artifactData.get().entries.joinToString("\n") { (baseName, filePath) ->
      "$baseName:$filePath"
    }
    file.writeText(content)
  }
}

val artifactsAndJarsFile = layout.buildDirectory.file("artifacts_and_jars.txt")

val writeArtifactsAndJars = tasks.register<WriteArtifactsAndJars>("writeArtifactsAndJars") {
  // Set up task dependencies
  dependsOn(jarTasks)

  // Configure the task inputs and outputs using providers
  artifactData.set(provider {
    jarTasks.associate { jar ->
      jar.archiveBaseName.get() to jar.archiveFile.get().asFile.absolutePath
    }
  })
  outputFile.set(artifactsAndJarsFile)
}

tasks {
  // This module depends on all published subprojects (for JaCoCo coverage aggregation and
  // arch tests). Since :exporters:http-sender:jdk targets Java 11, the compiler needs
  // release 11 to resolve its classes on the classpath.
  withType(JavaCompile::class) {
    options.release.set(11)
  }

  val testJavaVersion: String? by project
  test {
    enabled = testJavaVersion != "8"
    dependsOn(writeArtifactsAndJars)
    environment("ARTIFACTS_AND_JARS", artifactsAndJarsFile.get().asFile.absolutePath)
  }
}

// https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage.html

val sourcesPath by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
  extendsFrom(configurations.implementation.get())
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
    attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
  }
}

val coverageDataPath by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
  extendsFrom(configurations.implementation.get())
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
    attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("jacoco-coverage-data"))
  }
}

tasks.named<JacocoReport>("jacocoTestReport") {
  enabled = true

  dependsOn(testTasks)

  configurations.runtimeClasspath.get().forEach {
    additionalClassDirs(
      zipTree(it).filter {
        // Exclude mrjar (jacoco complains), shaded, and generated code
        !it.absolutePath.contains("META-INF/versions/") &&
          !it.absolutePath.contains("/internal/shaded/") &&
          !it.absolutePath.contains("io/opentelemetry/sdk/extension/trace/jaeger/proto/") &&
          !it.absolutePath.contains("AutoValue_")
      },
    )
  }
  additionalSourceDirs(sourcesPath.incoming.artifactView { lenient(true) }.files)
  executionData(coverageDataPath.incoming.artifactView { lenient(true) }.files.filter { it.exists() })

  reports {
    // xml is usually used to integrate code coverage with
    // other tools like SonarQube, Coveralls or Codecov
    xml.required.set(true)

    // HTML reports can be used to see code coverage
    // without any external tools
    html.required.set(true)
  }
}
