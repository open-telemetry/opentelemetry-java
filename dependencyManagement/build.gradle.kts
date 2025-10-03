plugins {
  `java-platform`
}

data class DependencySet(val group: String, val version: String, val modules: List<String>)

val dependencyVersions = hashMapOf<String, String>()
rootProject.extra["versions"] = dependencyVersions


val autoValueVersion = "1.11.0"
val errorProneVersion = "2.42.0"
val jmhVersion = "1.37"
// Mockito 5.x.x requires Java 11 https://github.com/mockito/mockito/releases/tag/v5.0.0
val mockitoVersion = "4.11.0"
val slf4jVersion = "2.0.17"
val opencensusVersion = "0.31.1"
val prometheusServerVersion = "1.3.10"
val armeriaVersion = "1.33.4"
val junitVersion = "5.13.4"
val okhttpVersion = "5.1.0"

val DEPENDENCY_BOMS = listOf(
  // for some reason boms show up as runtime dependencies in license and vulnerability scans
  // even if they are only used by test dependencies, so not using junit bom here
  // (which is EPL licensed) or armeria bom (which is Apache licensed but is getting flagged
  // by FOSSA for containing EPL-licensed)

  "com.fasterxml.jackson:jackson-bom:2.20.0",
  "com.google.guava:guava-bom:33.5.0-jre",
  "com.google.protobuf:protobuf-bom:4.32.1",
  "com.squareup.okhttp3:okhttp-bom:$okhttpVersion",
  "com.squareup.okio:okio-bom:3.16.0", // applies to transitive dependencies of okhttp
  "io.grpc:grpc-bom:1.75.0",
  "io.netty:netty-bom:4.2.6.Final",
  "io.zipkin.brave:brave-bom:6.3.0",
  "io.zipkin.reporter2:zipkin-reporter-bom:3.5.1",
  "org.assertj:assertj-bom:3.27.6",
  "org.testcontainers:testcontainers-bom:1.21.3",
  "org.snakeyaml:snakeyaml-engine:2.10"
)

val DEPENDENCIES = listOf(
  "org.junit.jupiter:junit-jupiter-api:${junitVersion}",
  "org.junit.jupiter:junit-jupiter-params:${junitVersion}",
  "com.linecorp.armeria:armeria:${armeriaVersion}",
  "com.linecorp.armeria:armeria-grpc:${armeriaVersion}",
  "com.linecorp.armeria:armeria-grpc-protocol:${armeriaVersion}",
  "com.linecorp.armeria:armeria-junit5:${armeriaVersion}",

  "com.google.auto.value:auto-value:${autoValueVersion}",
  "com.google.auto.value:auto-value-annotations:${autoValueVersion}",
  "com.google.errorprone:error_prone_annotations:${errorProneVersion}",
  "com.google.errorprone:error_prone_core:${errorProneVersion}",
  "com.google.errorprone:error_prone_test_helpers:${errorProneVersion}",
  "io.opencensus:opencensus-api:${opencensusVersion}",
  "io.opencensus:opencensus-impl-core:${opencensusVersion}",
  "io.opencensus:opencensus-impl:${opencensusVersion}",
  "io.opencensus:opencensus-exporter-metrics-util:${opencensusVersion}",
  "io.opencensus:opencensus-contrib-exemplar-util:${opencensusVersion}",
  "org.openjdk.jmh:jmh-core:${jmhVersion}",
  "org.openjdk.jmh:jmh-generator-bytecode:${jmhVersion}",
  "org.openjdk.jmh:jmh-generator-annprocess:${jmhVersion}",
  "org.mockito:mockito-core:${mockitoVersion}",
  "org.mockito:mockito-junit-jupiter:${mockitoVersion}",
  "org.slf4j:slf4j-simple:${slf4jVersion}",
  "org.slf4j:jul-to-slf4j:${slf4jVersion}",
  "io.prometheus:prometheus-metrics-exporter-httpserver:${prometheusServerVersion}",
  "io.prometheus:prometheus-metrics-exposition-formats-no-protobuf:${prometheusServerVersion}",
  "javax.annotation:javax.annotation-api:1.3.2",
  "com.github.stefanbirkner:system-rules:1.19.0",
  "com.google.api.grpc:proto-google-common-protos:2.61.3",
  "com.google.code.findbugs:jsr305:3.0.2",
  "com.google.guava:guava-beta-checker:1.0",
  "com.sun.net.httpserver:http:20070405",
  "com.squareup.okhttp3:okhttp:$okhttpVersion",
  "com.tngtech.archunit:archunit-junit5:1.4.1",
  "com.uber.nullaway:nullaway:0.12.10",
  "edu.berkeley.cs.jqf:jqf-fuzz:1.7", // jqf-fuzz version 1.8+ requires Java 11+
  "eu.rekawek.toxiproxy:toxiproxy-java:2.1.11",
  "io.github.netmikey.logunit:logunit-jul:2.0.0",
  "io.jaegertracing:jaeger-client:1.8.1",
  "io.opentelemetry.contrib:opentelemetry-aws-xray-propagator:1.50.0-alpha",
  "io.opentelemetry.semconv:opentelemetry-semconv-incubating:1.37.0-alpha",
  "io.opentelemetry.proto:opentelemetry-proto:1.8.0-alpha",
  "io.opentracing:opentracing-api:0.33.0",
  "io.opentracing:opentracing-noop:0.33.0",
  "junit:junit:4.13.2",
  "nl.jqno.equalsverifier:equalsverifier:3.19.4",
  "org.awaitility:awaitility:4.3.0",
  "org.bouncycastle:bcpkix-jdk15on:1.70",
  "org.codehaus.mojo:animal-sniffer-annotations:1.24",
  "org.jctools:jctools-core:4.0.5",
  "org.junit-pioneer:junit-pioneer:1.9.1",
  "org.mock-server:mockserver-netty:5.15.0:shaded",
  "org.skyscreamer:jsonassert:1.5.3",
  "com.android.tools:desugar_jdk_libs:2.1.5",
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
    for (dependency in DEPENDENCIES) {
      api(dependency)
      val split = dependency.split(':')
      dependencyVersions[split[0]] = split[2]
    }
  }
}
