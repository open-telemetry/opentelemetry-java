plugins {
  `kotlin-dsl`

  // When updating, update below in dependencies too
  id("com.diffplug.spotless") version "7.2.1"
}

if (!hasLauncherForJavaVersion(17)) {
  throw GradleException(
    "JDK 17 is required to build and gradle was unable to detect it on the system.  " +
        "Please install it and see https://docs.gradle.org/current/userguide/toolchains.html#sec:auto_detection " +
        "for details on how gradle detects java toolchains."
  )
}

fun hasLauncherForJavaVersion(version: Int): Boolean {
  return try {
    javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(version) }.get()
    true
  } catch (e: Exception) {
    false
  }
}

spotless {
  kotlinGradle {
    ktlint().editorConfigOverride(mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "2",
      "max_line_length" to "160",
      "insert_final_newline" to "true",
      "ktlint_standard_no-wildcard-imports" to "disabled",
      // ktlint does not break up long lines, it just fails on them
      "ktlint_standard_max-line-length" to "disabled",
      // ktlint makes it *very* hard to locate where this actually happened
      "ktlint_standard_trailing-comma-on-call-site" to "disabled",
      // depends on ktlint_standard_wrapping
      "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
      // also very hard to find out where this happens
      "ktlint_standard_wrapping" to "disabled"
    ))
    target("**/*.gradle.kts")
  }
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  mavenLocal()
}

dependencies {
  implementation(enforcedPlatform("com.squareup.wire:wire-bom:5.3.11"))
  implementation("com.google.auto.value:auto-value-annotations:1.11.0")
  // When updating, update above in plugins too
  implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
  implementation("com.gradle.develocity:com.gradle.develocity.gradle.plugin:4.1.1")
  implementation("com.squareup:javapoet:1.13.0")
  implementation("com.squareup.wire:wire-compiler")
  implementation("com.squareup.wire:wire-gradle-plugin")
  implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.18")
  implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.6")
  implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.6")
  implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.3")
  implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.3.0")
  implementation("net.ltgt.gradle:gradle-nullaway-plugin:2.3.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
  implementation("org.owasp:dependency-check-gradle:12.1.3")
  implementation("ru.vyarus:gradle-animalsniffer-plugin:2.0.1")
}

// We can't apply conventions to this build so include important ones such as the Java compilation
// target.
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
