plugins {
    id("otel.java-conventions")
    id("maven-publish")

    id("me.champeau.jmh")

    // TODO(anuraaga): Enable animalsniffer by the time we are getting ready to release a stable
    // version. Long/DoubleAdder are not part of Android API 21 which is our current target.
    // id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry SDK Metrics"
otelJava.moduleName.set("io.opentelemetry.sdk.metrics")

dependencies {
    api(project(":api:metrics"))
    api(project(":sdk:common"))

    annotationProcessor("com.google.auto.value:auto-value")

    testAnnotationProcessor("com.google.auto.value:auto-value")

    testImplementation(project(":sdk:testing"))
    testImplementation("com.google.guava:guava")
}

sourceSets {
    main {
        output.dir("build/generated/properties", "builtBy" to "generateVersionResource")
    }
}

tasks {
    register("generateVersionResource") {
        val propertiesDir = file("build/generated/properties/io/opentelemetry/sdk/metrics")
        outputs.dir(propertiesDir)

        doLast {
            File(propertiesDir, "version.properties").writeText("sdk.version=${project.version}")
        }
    }
}
