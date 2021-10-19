pluginManagement {
  plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.gradle.enterprise") version "3.7"
    id("de.undercouch.download") version "4.1.2"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("nebula.release") version "16.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("org.unbroken-dome.test-sets") version "4.0.0"
  }
}

plugins {
  id("com.gradle.enterprise")
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
include(":extensions:incubator")
include(":extensions:aws")
include(":extensions:kotlin")
include(":extensions:noop-api")
include(":extensions:trace-propagators")
include(":exporters:jaeger")
include(":exporters:jaeger-proto")
include(":exporters:jaeger-thrift")
include(":exporters:logging")
include(":exporters:logging-otlp")
include(":exporters:otlp:all")
include(":exporters:otlp:common")
include(":exporters:otlp:logs")
include(":exporters:otlp:metrics")
include(":exporters:otlp:trace")
include(":exporters:otlp-http:logs")
include(":exporters:otlp-http:metrics")
include(":exporters:otlp-http:trace")
include(":exporters:prometheus")
include(":exporters:zipkin")
include(":integration-tests")
include(":integration-tests:tracecontext")
include(":opencensus-shim")
include(":opentracing-shim")
include(":perf-harness")
include(":sdk:all")
include(":sdk:common")
include(":sdk:logs")
include(":sdk:metrics")
include(":sdk:metrics-testing")
include(":sdk:testing")
include(":sdk:trace")
include(":sdk:trace-shaded-deps")
include(":sdk-extensions:autoconfigure")
include(":sdk-extensions:autoconfigure-spi")
include(":sdk-extensions:aws")
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
