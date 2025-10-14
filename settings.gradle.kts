pluginManagement {
  plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("com.gradle.develocity") version "4.2.2"
    id("de.undercouch.download") version "5.6.0"
    id("org.jsonschema2pojo") version "1.2.2"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.graalvm.buildtools.native") version "0.10.6"
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity")
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
include(":api:testing-internal")
include(":bom")
include(":bom-alpha")
include(":common")
include(":context")
include(":custom-checks")
include(":dependencyManagement")
include(":extensions:kotlin")
include(":extensions:trace-propagators")
include(":exporters:common")
include(":exporters:common:compile-stub")
include(":exporters:sender:grpc-managed-channel")
include(":exporters:sender:jdk")
include(":exporters:sender:okhttp")
include(":exporters:logging")
include(":exporters:logging-otlp")
include(":exporters:otlp:all")
include(":exporters:otlp:common")
include(":exporters:otlp:profiles")
include(":exporters:otlp:testing-internal")
include(":exporters:prometheus")
include(":exporters:zipkin")
include(":integration-tests")
include(":integration-tests:otlp")
include(":integration-tests:tracecontext")
include(":integration-tests:graal")
include(":integration-tests:graal-incubating")
include(":javadoc-crawler")
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

develocity {
  buildScan {
    publishing.onlyIf { System.getenv("CI") != null }
    termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
    termsOfUseAgree.set("yes")
  }
}
