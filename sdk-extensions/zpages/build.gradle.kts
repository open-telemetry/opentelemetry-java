plugins {
    `java-library`
    `maven-publish`

    id("me.champeau.gradle.jmh")
}

description = "OpenTelemetry - zPages"
extra["moduleName"] = "io.opentelemetry.sdk.extension.zpages"

dependencies {
    implementation(project(":api:all"))
    implementation(project(":sdk:all"))

    testImplementation("com.google.guava:guava")
    
    compileOnly("com.sun.net.httpserver:http")
}
