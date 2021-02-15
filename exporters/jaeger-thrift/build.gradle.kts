plugins {
    `java-library`
    `maven-publish`

    id("ru.vyarus.animalsniffer")
}

description = "OpenTelemetry - Jaeger Thrift Exporter"
extra["moduleName"] = "io.opentelemetry.exporter.jaeger.thrift"

dependencies {
    api(project(":sdk:all"))

    implementation(project(":sdk:all"))

    implementation("io.jaegertracing:jaeger-client")

    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("com.google.guava:guava-testlib")
    
    testImplementation(project(":sdk:testing"))
}
