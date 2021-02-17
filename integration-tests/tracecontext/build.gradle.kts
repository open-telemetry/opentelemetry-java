plugins {
    java

    id("com.github.johnrengelman.shadow")
}

description = "OpenTelemetry W3C Context Propagation Integration Tests"
extra["moduleName"] = "io.opentelemetry.tracecontext.integration.tests"

dependencies {
    implementation(project(":sdk:all"))
    implementation(project(":extensions:trace-propagators"))

    implementation("com.squareup.okhttp3:okhttp")
    implementation("org.slf4j:slf4j-simple")
    implementation("com.sparkjava:spark-core")
    implementation("com.google.code.gson:gson")
}

tasks {
    val shadowJar by existing(Jar::class) {
        archiveFileName.set("tracecontext-tests.jar")

        manifest {
            attributes("Main-Class" to "io.opentelemetry.Application")
        }
    }
}
