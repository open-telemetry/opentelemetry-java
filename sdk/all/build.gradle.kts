plugins {
    id("otel.java-conventions")
    id("maven-publish")

    id("me.champeau.jmh")
    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry SDK"
otelJava.moduleName.set("io.opentelemetry.sdk")
base.archivesBaseName = "opentelemetry-sdk"

dependencies {
    api(project(":api:all"))
    api(project(":sdk:common"))
    api(project(":sdk:trace"))

    annotationProcessor("com.google.auto.value:auto-value")

    testAnnotationProcessor("com.google.auto.value:auto-value")

    testImplementation(project(":sdk:testing"))
}

sourceSets {
    main {
        output.dir("build/generated/properties", "builtBy" to "generateVersionResource")
    }
}

tasks {
    register("generateVersionResource") {
        val propertiesDir = file("build/generated/properties/io/opentelemetry/sdk")
        outputs.dir(propertiesDir)

        doLast {
            File(propertiesDir, "version.properties").writeText("sdk.version=${project.version}")
        }
    }
}
