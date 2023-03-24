plugins {
  `kotlin-dsl`

  // When updating, update below in dependencies too
  id("com.diffplug.spotless") version "6.17.0"
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
  implementation("com.google.auto.value:auto-value-annotations:1.10.1")
  // When updating, update above in plugins too
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.17.0")
  // Needed for japicmp but not automatically brought in for some reason.
  implementation("com.google.guava:guava:31.1-jre")
  implementation("com.squareup:javapoet:1.13.0")
  implementation("com.squareup.wire:wire-compiler:4.5.2")
  implementation("com.squareup.wire:wire-gradle-plugin:4.5.2")
  implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.18")
  implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.0")
  implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.1")
  implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.0")
  implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.0.1")
  implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.5.0")
  implementation("ru.vyarus:gradle-animalsniffer-plugin:1.7.0")
  implementation("org.owasp:dependency-check-gradle:8.2.1")
}

// We can't apply conventions to this build so include important ones such as the Java compilation
// target.
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
