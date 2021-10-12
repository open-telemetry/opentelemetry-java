plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  mavenLocal()
}

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:5.15.2")
  // Needed for japicmp but not automatically brought in for some reason.
  implementation("com.google.guava:guava:30.1.1-jre")
  implementation("com.squareup:javapoet:1.13.0")
  implementation("com.squareup.wire:wire-compiler:3.7.0")
  implementation("com.squareup.wire:wire-gradle-plugin:3.7.0")
  implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.17")
  implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.0")
  implementation("me.champeau.gradle:japicmp-gradle-plugin:0.3.0")
  implementation("me.champeau.jmh:jmh-gradle-plugin:0.6.6")
  implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
  implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.1.0")
  implementation("ru.vyarus:gradle-animalsniffer-plugin:1.5.3")
}
