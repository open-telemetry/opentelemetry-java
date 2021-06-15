plugins {
    id("otel.java-conventions")
    `maven-publish`
}

description = "OpenTelemetry SDK Extension JFR"
extra["moduleName"] = "io.opentelemetry.sdk.extension.jfr"

dependencies {
    implementation(project(":api:all"))
    implementation(project(":sdk:all"))
}

tasks {
    withType(JavaCompile::class) {
        options.release.set(11)
    }

    named("testJava8") {
        enabled = false
    }

    named("test") {
        // Disabled due to https://bugs.openjdk.java.net/browse/JDK-8245283
        configure<JacocoTaskExtension> {
            enabled = false
        }
    }
}
