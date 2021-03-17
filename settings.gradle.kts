pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "5.9.0"
        id("com.github.ben-manes.versions") version "0.36.0"
        id("com.github.johnrengelman.shadow") version "6.1.0"
        id("com.google.protobuf") version "0.8.14"
        id("de.marcphilipp.nexus-publish") version "0.4.0"
        id("de.undercouch.download") version "4.1.1"
        id("io.codearte.nexus-staging") version "0.22.0"
        id("io.morethan.jmhreport") version "0.9.0"
        id("me.champeau.gradle.jmh") version "0.5.3"
        id("nebula.release") version "15.3.0"
        id("net.ltgt.errorprone") version "1.3.0"
        id("org.jetbrains.kotlin.jvm") version "1.4.21"
        id("org.unbroken-dome.test-sets") version "3.0.1"
        id("ru.vyarus.animalsniffer") version "1.5.2"
    }
}

plugins {
    id("com.gradle.enterprise") version "3.5"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "opentelemetry-java"
include(":all")
include(":api:all")
include(":api:metrics")
include(":semconv")
include(":bom")
include(":bom-alpha")
include(":context")
include(":dependencyManagement")
include(":extensions:annotations")
include(":extensions:aws")
include(":extensions:kotlin")
include(":extensions:trace-propagators")
include(":exporters:jaeger")
include(":exporters:jaeger-thrift")
include(":exporters:logging")
include(":exporters:logging-otlp")
include(":exporters:otlp:all")
include(":exporters:otlp:common")
include(":exporters:otlp:metrics")
include(":exporters:otlp:trace")
include(":exporters:prometheus")
include(":exporters:zipkin")
include(":integration-tests")
include(":integration-tests:tracecontext")
include(":opencensus-shim")
include(":opentracing-shim")
include(":perf-harness")
include(":proto")
include(":sdk:all")
include(":sdk:common")
include(":sdk:metrics")
include(":sdk:testing")
include(":sdk:trace")
include(":sdk:trace-shaded-deps")
include(":sdk-extensions:async-processor")
include(":sdk-extensions:autoconfigure")
include(":sdk-extensions:aws")
include(":sdk-extensions:logging")
include(":sdk-extensions:resources")
include(":sdk-extensions:tracing-incubator")
include(":sdk-extensions:jaeger-remote-sampler")
include(":sdk-extensions:jfr-events")
include(":sdk-extensions:zpages")

val isCI = System.getenv("CI") != null
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        if (isCI) {
            publishAlways()
            tag("CI")
        }
    }
}
