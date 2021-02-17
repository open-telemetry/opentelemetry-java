plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry SDK Resource Providers"
extra["moduleName"] = "io.opentelemetry.sdk.extension.resources"

dependencies {
    api(project(":sdk:common"))
    api(project(":semconv"))

    compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

    testImplementation("org.junit-pioneer:junit-pioneer")
}
