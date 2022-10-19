plugins {
  `kotlin-dsl`

  // When updating, update below in dependencies too
  id("com.diffplug.spotless") version "6.11.0"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  mavenLocal()
}

dependencies {
  implementation("com.google.auto.value:auto-value-annotations:1.10")
  // When updating, update above in plugins too
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.11.0")
  // Needed for japicmp but not automatically brought in for some reason.
  implementation("com.google.guava:guava:31.1-jre")
  implementation("com.squareup:javapoet:1.13.0")
  implementation("com.squareup.wire:wire-compiler:4.4.2")
  implementation("com.squareup.wire:wire-gradle-plugin:4.4.2")
  implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.18")
  implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.0")
  implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.1")
  implementation("me.champeau.jmh:jmh-gradle-plugin:0.6.8")
  implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.0.1")
  implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.4.0")
  implementation("ru.vyarus:gradle-animalsniffer-plugin:1.6.0")
}

// We can't apply conventions to this build so include important ones such as the Java compilation
// target.
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    with(options) {
      release.set(8)
    }
  }
}

spotless {
  kotlinGradle {
    ktlint().editorConfigOverride(mapOf("indent_size" to "2", "continuation_indent_size" to "2", "disabled_rules" to "no-wildcard-imports"))
    target("**/*.gradle.kts")
  }
}
