pluginManagement {
  plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.gradle.enterprise") version "3.6"
    id("de.undercouch.download") version "4.1.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("nebula.release") version "15.3.1"
    id("org.checkerframework") version "0.5.20"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
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
include(":api:opentelemetry-api")
include(":api:opentelemetry-api-metrics")
include(":opentelemetry-semconv")
include(":opentelemetry-bom")
include(":opentelemetry-bom-alpha")
include(":opentelemetry-context")
include(":dependencyManagement")
include(":extensions:opentelemetry-extension-annotations")
include(":extensions:opentelemetry-extension-incubator")
include(":extensions:opentelemetry-extension-aws")
include(":extensions:opentelemetry-extension-kotlin")
include(":extensions:opentelemetry-extension-noop-api")
include(":extensions:opentelemetry-extension-trace-propagators")
include(":exporters:opentelemetry-exporter-jaeger")
include(":exporters:opentelemetry-exporter-jaeger-thrift")
include(":exporters:opentelemetry-exporter-logging")
include(":exporters:opentelemetry-exporter-logging-otlp")
include(":exporters:otlp:opentelemetry-exporter-otlp")
include(":exporters:otlp:opentelemetry-exporter-otlp-common")
include(":exporters:otlp:opentelemetry-exporter-otlp-metrics")
include(":exporters:otlp:opentelemetry-exporter-otlp-trace")
include(":exporters:otlp-http:opentelemetry-exporter-otlp-http-metrics")
include(":exporters:otlp-http:opentelemetry-exporter-otlp-http-trace")
include(":exporters:opentelemetry-exporter-prometheus")
include(":exporters:opentelemetry-exporter-zipkin")
include(":integration-tests")
include(":integration-tests:tracecontext")
include(":opentelemetry-opencensus-shim")
include(":opentelemetry-opentracing-shim")
include(":perf-harness")
include(":opentelemetry-proto")
include(":sdk:opentelemetry-sdk")
include(":sdk:opentelemetry-sdk-common")
include(":sdk:opentelemetry-sdk-metrics")
include(":sdk:opentelemetry-sdk-metrics-testing")
include(":sdk:opentelemetry-sdk-testing")
include(":sdk:opentelemetry-sdk-trace")
include(":sdk:sdk-trace-shaded-deps")
include(":sdk-extensions:opentelemetry-sdk-extension-async-processor")
include(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure")
include(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure-spi")
include(":sdk-extensions:opentelemetry-sdk-extension-aws")
include(":sdk-extensions:opentelemetry-sdk-extension-logging")
include(":sdk-extensions:opentelemetry-sdk-extension-resources")
include(":sdk-extensions:opentelemetry-sdk-extension-tracing-incubator")
include(":sdk-extensions:opentelemetry-sdk-extension-jaeger-remote-sampler")
include(":sdk-extensions:opentelemetry-sdk-extension-jfr-events")
include(":sdk-extensions:opentelemetry-sdk-extension-zpages")

// Map project names to directory names.
fun patchNames(rootProject: ProjectDescriptor) {
  rootProject.children.forEach {
    if (it.name.startsWith("opentelemetry-")) {
      val pathParts = it.path.split(":").toMutableList()
      pathParts.removeFirst() // Leading ":" gives us an empty path component, leading to weirdness
      val prefix = "opentelemetry-" + pathParts.subList(0, pathParts.lastIndex).joinToString("-")
      val singularPrefix = "opentelemetry-" + pathParts
        .subList(0, pathParts.lastIndex)
        .joinToString("-") { s -> s.removeSuffix("s") }
      pathParts[pathParts.lastIndex] = pathParts.last()
        .replaceFirst(prefix, "")
        .replaceFirst(singularPrefix, "")
        .removePrefix("-")
        .ifEmpty { "all" }
      it.projectDir = file(pathParts.joinToString("/"))
    }
    patchNames(it)
  }
}
patchNames(rootProject)

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
