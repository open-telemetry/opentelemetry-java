plugins {
    id("otel.java-conventions")
    `maven-publish`
}

description = "OpenTelemetry OpenCensus Shim"
extra["moduleName"] = "io.opentelemetry.opencensusshim"

dependencies {
    api(project(":api:all"))
    api(project(":extensions:trace-propagators"))
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    api("io.opencensus:opencensus-api")
    api("io.opencensus:opencensus-impl-core")
    api("io.opencensus:opencensus-exporter-metrics-util")

    testImplementation(project(":sdk:all"))

    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("io.opencensus:opencensus-impl")
}
