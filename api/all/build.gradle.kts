plugins {
    id("java-library")
    id("maven-publish")

    id("me.champeau.gradle.jmh")
    id("org.unbroken-dome.test-sets")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry API"
extra["moduleName"] = "io.opentelemetry.api"
base.archivesBaseName = "opentelemetry-api"

testSets {
    create("testLogsIfSdkFound")
    create("testDoesNotLogIfSdkFoundAndSuppressed")
}

dependencies {
    api(project(":context"))

    annotationProcessor("com.google.auto.value:auto-value")

    testImplementation("edu.berkeley.cs.jqf:jqf-fuzz")
    testImplementation("com.google.guava:guava-testlib")
}

tasks {
    named<Test>("testDoesNotLogIfSdkFoundAndSuppressed") {
        jvmArgs("-Dotel.sdk.suppress-sdk-initialized-warning=true")
    }
}