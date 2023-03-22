pluginManagement {
  plugins {
    id("com.github.ben-manes.versions") version "0.46.0"
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("com.gradle.enterprise") version "3.12.5"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.graalvm.buildtools.native") version "0.9.20"
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
include(":api:events")
include(":api:logs")
include(":semconv")
include(":bom")
include(":bom-alpha")
include(":context")
include(":dependencyManagement")
include(":extensions:incubator")
include(":extensions:kotlin")
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
include(":integration-tests:graal")
include(":opencensus-shim")
include(":opentracing-shim")
include(":perf-harness")
include(":sdk:all")
include(":sdk:common")
include(":sdk:logs")
include(":sdk:logs-testing")
include(":sdk:metrics")
include(":sdk:testing")
include(":sdk:trace")
include(":sdk:trace-shaded-deps")
include(":sdk-extensions:autoconfigure")
include(":sdk-extensions:autoconfigure-spi")
include(":sdk-extensions:incubator")
include(":sdk-extensions:jaeger-remote-sampler")
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
