plugins {
  `kotlin-dsl`

  // When updating, update below in dependencies too
  id("com.diffplug.spotless") version "8.4.0"
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
  // TODO: delete bndtools repository once biz.aQute.bnd.gradle:7.3.0 is released
  maven { url = uri("https://bndtools.jfrog.io/artifactory/libs-release/") }
  mavenLocal()
}

// TODO: delete version pinning once biz.aQute.bnd.gradle:7.3.0 is released
configurations.all {
  resolutionStrategy.eachDependency {
    // biz.aQute.bnd.gradle 7.3.0-RC1 transitives are not all published at RC1; pin to latest stable
    if (requested.group == "biz.aQute.bnd" && requested.name != "biz.aQute.bnd.gradle") {
      useVersion("7.2.3")
    }
  }
}

dependencies {
  implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:7.3.0-RC1")
  implementation(enforcedPlatform("com.squareup.wire:wire-bom:6.2.0"))
  implementation("com.google.auto.value:auto-value-annotations:1.11.1")
  // When updating, update above in plugins too
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.4.0")
  implementation("com.gradle.develocity:com.gradle.develocity.gradle.plugin:4.4.1")
  implementation("com.squareup:javapoet:1.13.0")
  implementation("com.squareup.wire:wire-compiler")
  implementation("com.squareup.wire:wire-gradle-plugin")
  implementation("com.google.protobuf:protobuf-gradle-plugin:0.10.0")
  implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.6")
  implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.6")
  implementation("me.champeau.jmh:jmh-gradle-plugin:0.7.3")
  implementation("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
  implementation("net.ltgt.gradle:gradle-nullaway-plugin:3.0.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
  implementation("org.sonatype.gradle.plugins:scan-gradle-plugin:3.1.5")
  implementation("ru.vyarus:gradle-animalsniffer-plugin:2.0.1")
}
