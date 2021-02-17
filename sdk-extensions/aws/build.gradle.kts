plugins {
    `java-library`
    `maven-publish`

    id("me.champeau.gradle.jmh")
}

description = "OpenTelemetry SDK AWS Instrumentation Support"
extra["moduleName"] = "io.opentelemetry.sdk.extension.trace.aws"

dependencies {
    api(project(":api:all"))
    api(project(":sdk:all"))

    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("com.linecorp.armeria:armeria-junit5")
    testImplementation("com.google.guava:guava")
}
