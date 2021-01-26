plugins {
    `java-platform`
}

data class DependencySet(val group: String, val version: String, val modules: List<String>)

val dependencyVersions = hashMapOf<String, String>()
rootProject.extra["versions"] = dependencyVersions

val DEPENDENCY_BOMS = listOf(
        "com.linecorp.armeria:armeria-bom:1.3.0",
        "io.grpc:grpc-bom:1.34.1",
        "io.zipkin.brave:brave-bom:5.13.1",
        "com.google.guava:guava-bom:30.1-jre",
        "com.google.protobuf:protobuf-bom:3.14.0",
        "com.fasterxml.jackson:jackson-bom:2.12.0",
        "org.junit:junit-bom:5.7.0",
        "io.zipkin.reporter2:zipkin-reporter-bom:2.16.3"
)

val DEPENDENCY_SETS = listOf(
        DependencySet(
                "com.google.auto.value",
                "1.7.4",
                listOf("auto-value", "auto-value-annotations")
        ),
        DependencySet(
                "com.google.errorprone",
                "2.4.0",
                listOf("error_prone_annotations", "error_prone_core")
        ),
        DependencySet(
                "io.opencensus",
                "0.28.2",
                listOf(
                        "opencensus-api",
                        "opencensus-impl-core",
                        "opencensus-impl",
                        "opencensus-exporter-metrics-util"
                )
        ),
        DependencySet(
                "io.prometheus",
                "0.9.0",
                listOf("simpleclient", "simpleclient_common", "simpleclient_httpserver")
        ),
        DependencySet(
                "javax.annotation",
                "1.3.2",
                listOf("javax.annotation-api")
        ),
        DependencySet(
                "org.openjdk.jmh",
                "1.26",
                listOf("jmh-core", "jmh-generator-bytecode")
        ),
        DependencySet(
                "org.mockito",
                "3.7.0",
                listOf("mockito-core", "mockito-junit-jupiter")
        )
)

val DEPENDENCIES = listOf(
        "com.github.stefanbirkner:system-rules:1.19.0",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.6",
        "com.google.guava:guava-beta-checker:1.0",
        "com.lmax:disruptor:3.4.2",
        "com.sparkjava:spark-core:2.9.3",
        "com.squareup.okhttp3:okhttp:3.14.9",
        "com.sun.net.httpserver:http:20070405",
        "com.tngtech.archunit:archunit-junit4:0.15.0",
        "edu.berkeley.cs.jqf:jqf-fuzz:1.6",
        "eu.rekawek.toxiproxy:toxiproxy-java:2.1.4",
        "io.github.netmikey.logunit:logunit-jul:1.1.0",
        "io.jaegertracing:jaeger-client:1.5.0",
        "io.opentracing:opentracing-api:0.33.0",
        "io.zipkin.zipkin2:zipkin-junit:2.18.3",
        "junit:junit:4.13.1",
        "nl.jqno.equalsverifier:equalsverifier:3.5",
        "org.assertj:assertj-core:3.18.1",
        "org.awaitility:awaitility:4.0.3",
        "org.curioswitch.curiostack:protobuf-jackson:1.1.0",
        "org.junit-pioneer:junit-pioneer:1.1.0",
        "org.skyscreamer:jsonassert:1.5.0",
        "org.slf4j:slf4j-simple:1.7.30",
        "org.testcontainers:junit-jupiter:1.15.1"
)

javaPlatform {
    allowDependencies()
}

dependencies {
    for (bom in DEPENDENCY_BOMS) {
        api(enforcedPlatform(bom))
        val split = bom.split(':')
        dependencyVersions[split[0]] = split[2]
    }
    constraints {
        for (set in DEPENDENCY_SETS) {
            for (module in set.modules) {
                api("${set.group}:${module}:${set.version}")
                dependencyVersions[set.group] = set.version
            }
        }
        for (dependency in DEPENDENCIES) {
            api(dependency)
            val split = dependency.split(':')
            dependencyVersions[split[0]] = split[2]
        }
    }
}
