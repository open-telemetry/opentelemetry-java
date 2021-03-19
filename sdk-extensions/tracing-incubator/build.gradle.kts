plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

// SDK modules that are still being developed.

description = "OpenTelemetry SDK Tracing Incubator"
extra["moduleName"] = "io.opentelemetry.sdk.extension.trace.incubator"

dependencies {
    api(project(":api:all"))
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    implementation(project(":semconv"))

    annotationProcessor("com.google.auto.value:auto-value")
    testImplementation(project(":sdk:testing"))
    testImplementation("com.google.guava:guava-testlib")
}