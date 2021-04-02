plugins {
    java

    id("com.github.johnrengelman.shadow")
}

description = "OpenTelemetry W3C Context Propagation Integration Tests"
extra["moduleName"] = "io.opentelemetry.tracecontext.integration.tests"

dependencies {
    implementation(project(":sdk:all"))
    implementation(project(":extensions:trace-propagators"))

    implementation("com.linecorp.armeria:armeria")
    implementation("com.google.code.gson:gson")
    implementation("org.slf4j:slf4j-simple")
}

tasks {
    val shadowJar by existing(Jar::class) {
        archiveFileName.set("tracecontext-tests.jar")

        manifest {
            attributes("Main-Class" to "io.opentelemetry.Application")
        }
    }
}
