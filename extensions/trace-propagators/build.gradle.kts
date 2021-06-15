plugins {
    id("otel.java-conventions")
    `maven-publish`

    id("ru.vyarus.animalsniffer")
    id("me.champeau.jmh")
}

description = "OpenTelemetry Extension : Trace Propagators"
extra["moduleName"] = "io.opentelemetry.extension.trace.propagation"

dependencies {
    api(project(":api:all"))

    compileOnly(project(":sdk-extensions:autoconfigure"))

    testImplementation("io.jaegertracing:jaeger-client")
    testImplementation("com.google.guava:guava")

    jmhImplementation(project(":extensions:aws"))
}
