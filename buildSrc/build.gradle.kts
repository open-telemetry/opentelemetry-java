plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.13.0")
    implementation("gradle.plugin.io.morethan.jmhreport:gradle-jmh-report:0.9.0")
    implementation("me.champeau.jmh:jmh-gradle-plugin:0.6.5")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.1")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.1.0")
}
