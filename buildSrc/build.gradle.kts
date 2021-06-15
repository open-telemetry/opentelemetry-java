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
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:2.0.1")
    implementation("net.ltgt.gradle:gradle-nullaway-plugin:1.1.0")
}
