plugins {
  id("otel.java-conventions")
}

description = "OpenTelemetry All"
otelJava.moduleName.set("io.opentelemetry.all")

tasks {
  // We don't compile much here, just some API boundary tests. This project is mostly for
  // aggregating jacoco reports and it doesn't work if this isn't at least as high as the
  // highest supported Java version in any of our projects. All of our
  // projects target Java 8 except :exporters:http-sender:jdk, which targets
  // Java 11
  withType(JavaCompile::class) {
    options.release.set(11)
  }

  val testJavaVersion: String? by project
  if (testJavaVersion == "8") {
    test {
      enabled = false
    }
  }
}

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}

val testTasks = mutableListOf<Task>()

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
      }
    }
  }

  testImplementation("com.tngtech.archunit:archunit-junit5")
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
