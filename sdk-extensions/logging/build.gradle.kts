plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry Contrib Logging Support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.logging")

dependencies {
    api(project(":sdk:all"))

    implementation(project(":api:metrics"))

    implementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.awaitility:awaitility")

    annotationProcessor("com.google.auto.value:auto-value")
}
