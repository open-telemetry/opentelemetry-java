pluginManagement {
  plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.gradle.enterprise") version "3.17"
    id("de.undercouch.download") version "5.6.0"
    id("org.jsonschema2pojo") version "1.2.1"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.graalvm.buildtools.native") version "0.10.1"
  }
}

plugins {
  id("com.gradle.enterprise")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    mavenLocal()
  }
}

rootProject.name = "opentelemetry-java"
include(":all")
include(":api:all")
include(":api:incubator")
include(":bom")
include(":bom-alpha")
include(":context")
include(":dependencyManagement")
include(":extensions:kotlin")
include(":extensions:trace-propagators")
include(":exporters:common")
include(":exporters:sender:grpc-managed-channel")
include(":exporters:sender:jdk")
include(":exporters:sender:okhttp")
include(":exporters:logging")
include(":exporters:logging-otlp")
include(":exporters:otlp:all")
include(":exporters:otlp:common")
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
include(":sdk:metrics")
include(":sdk:testing")
include(":sdk:trace")
include(":sdk:trace-shaded-deps")
include(":sdk-extensions:autoconfigure")
include(":sdk-extensions:autoconfigure-spi")
include(":sdk-extensions:incubator")
include(":sdk-extensions:jaeger-remote-sampler")
include(":testing-internal")
include(":animal-sniffer-signature")

val gradleEnterpriseServer = "https://ge.opentelemetry.io"
val isCI = System.getenv("CI") != null
val geAccessKey = System.getenv("GRADLE_ENTERPRISE_ACCESS_KEY") ?: ""

// if GE access key is not given and we are in CI, then we publish to scans.gradle.com
val useScansGradleCom = isCI && geAccessKey.isEmpty()

if (useScansGradleCom) {
  gradleEnterprise {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
      isUploadInBackground = !isCI
      publishAlways()

      capture {
        isTaskInputFiles = true
      }
    }
  }
} else {
  gradleEnterprise {
    server = gradleEnterpriseServer
    buildScan {
      isUploadInBackground = !isCI

      this as com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures
      publishIfAuthenticated()
      publishAlways()

      capture {
        isTaskInputFiles = true
      }

      gradle.startParameter.projectProperties["testJavaVersion"]?.let { tag(it) }
      gradle.startParameter.projectProperties["testJavaVM"]?.let { tag(it) }
    }
  }
}
