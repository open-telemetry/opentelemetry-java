plugins {
  id("otel.java-conventions")
}

dependencies {
  compileOnly("com.google.errorprone:error_prone_core")

  testImplementation("com.google.errorprone:error_prone_test_helpers")
}

otelJava.moduleName.set("io.opentelemetry.javaagent.customchecks")

// We cannot use "--release" javac option here because that will forbid exporting com.sun.tools package.
// We also can't seem to use the toolchain without the "--release" option. So disable everything.

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
  toolchain {
    languageVersion.set(null as JavaLanguageVersion?)
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    with(options) {
      release.set(null as Int?)

      compilerArgs.addAll(
        listOf(
          "--add-exports",
          "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
          "--add-exports",
          "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
          "--add-exports",
          "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
          "--add-exports",
          "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
          "--add-exports",
          "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        ),
      )
    }
  }

  // only test on java 17+
  val testJavaVersion: String? by project
  if (testJavaVersion != null && Integer.valueOf(testJavaVersion) < 17) {
    test {
      enabled = false
    }
  }
}

tasks.withType<Test>().configureEach {
  // required on jdk17
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
  jvmArgs("--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
  jvmArgs("-XX:+IgnoreUnrecognizedVMOptions")
}

tasks.withType<Javadoc>().configureEach {
  // using com.sun.tools.javac.api.JavacTrees breaks javadoc generation
  enabled = false
}

// Our conventions apply this project as a dependency in the errorprone configuration, which would cause
// a circular dependency if trying to compile this project with that still there. So we filter this
// project out.
configurations {
  named("errorprone") {
    dependencies.removeIf {
      it is ProjectDependency && it.name == project.name
    }
  }
}

// Skip OWASP dependencyCheck task on test module
dependencyCheck {
  skip = true
}
