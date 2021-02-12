plugins {
    `java-library`
    `maven-publish`

    // TODO(anuraaga): Use reflection for RuntimeMXBean which is not included in Android.
    // id "ru.vyarus.animalsniffer"
}

description = "OpenTelemetry SDK Resource Providers"
extra["moduleName"] = "io.opentelemetry.sdk.extension.resources"

dependencies {
    api(project(":sdk:common"))
    api(project(":semconv"))

    testImplementation("org.junit-pioneer:junit-pioneer")
}
