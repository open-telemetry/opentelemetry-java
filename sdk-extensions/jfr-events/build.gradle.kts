plugins {
    id("otel.java-conventions")
    id("otel.publish-conventions")
}

description = "OpenTelemetry SDK Extension JFR"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.jfr")

dependencies {
    implementation(project(":api:all"))
    implementation(project(":sdk:all"))
}

tasks {
    withType(JavaCompile::class) {
        options.release.set(11)
    }

    test {
        val testJavaVersion: String? by project
        if (testJavaVersion == "8") {
            enabled = false
        }

        // Disabled due to https://bugs.openjdk.java.net/browse/JDK-8245283
        configure<JacocoTaskExtension> {
            enabled = false
        }
    }
}
