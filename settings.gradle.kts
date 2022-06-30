pluginManagement {
  plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.gradle.enterprise") version "3.9"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
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
include(":exporters:common")
include(":exporters:jaeger")
include(":exporters:jaeger-proto")
include(":exporters:jaeger-thrift")
include(":exporters:logging")
include(":exporters:logging-otlp")
include(":exporters:otlp:all")
include(":exporters:otlp:common")
include(":exporters:otlp:logs")
include(":exporters:otlp:testing-internal")
include(":exporters:prometheus")
include(":exporters:zipkin")
include(":integration-tests")
include(":integration-tests:otlp")
include(":integration-tests:tracecontext")
include(":micrometer1-shim")
include(":opencensus-shim")
include(":opentracing-shim")
include(":perf-harness")
include(":sdk:all")
include(":sdk:common")
include(":sdk:logs")
include(":sdk:logs-testing")
include(":sdk:metrics")
include(":sdk:metrics-testing")
include(":sdk:testing")
include(":sdk:trace")
include(":sdk:trace-shaded-deps")
include(":sdk-extensions:autoconfigure")
include(":sdk-extensions:autoconfigure-spi")
include(":sdk-extensions:aws")
include(":sdk-extensions:metric-incubator")
include(":sdk-extensions:resources")
include(":sdk-extensions:tracing-incubator")
include(":sdk-extensions:jaeger-remote-sampler")
include(":sdk-extensions:jfr-events")
include(":sdk-extensions:zpages")
include(":testing-internal")

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
