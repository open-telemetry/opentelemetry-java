import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  `java-platform`

  id("com.github.ben-manes.versions")
}

data class DependencySet(val group: String, val version: String, val modules: List<String>)

val dependencyVersions = hashMapOf<String, String>()
rootProject.extra["versions"] = dependencyVersions

val DEPENDENCY_BOMS = listOf(
  "com.fasterxml.jackson:jackson-bom:2.13.0",
  "com.google.guava:guava-bom:31.0.1-jre",
  "com.google.protobuf:protobuf-bom:3.18.1",
  "com.linecorp.armeria:armeria-bom:1.13.4",
  "com.squareup.okhttp3:okhttp-bom:4.9.3",
  "io.grpc:grpc-bom:1.42.1",
  "io.zipkin.brave:brave-bom:5.13.3",
  "io.zipkin.reporter2:zipkin-reporter-bom:2.16.3",
  "org.junit:junit-bom:5.8.2",
  "org.testcontainers:testcontainers-bom:1.16.2"
)

val DEPENDENCY_SETS = listOf(
  DependencySet(
    "com.google.auto.value",
    "1.8.2",
    listOf("auto-value", "auto-value-annotations")
  ),
  DependencySet(
    "com.google.errorprone",
    "2.9.0",
    listOf("error_prone_annotations", "error_prone_core")
  ),
  DependencySet(
    "io.opencensus",
    "0.28.3",
    listOf(
      "opencensus-api",
      "opencensus-impl-core",
      "opencensus-impl",
      "opencensus-exporter-metrics-util",
      "opencensus-contrib-exemplar-util"
    )
  ),
  DependencySet(
    "io.prometheus",
    "0.12.0",
    listOf("simpleclient", "simpleclient_common", "simpleclient_httpserver")
  ),
  DependencySet(
    "javax.annotation",
    "1.3.2",
    listOf("javax.annotation-api")
  ),
  DependencySet(
    "org.openjdk.jmh",
    "1.33",
    listOf("jmh-core", "jmh-generator-bytecode")
  ),
  DependencySet(
    "org.mockito",
    "4.1.0",
    listOf("mockito-core", "mockito-junit-jupiter")
  )
)

val DEPENDENCIES = listOf(
  "com.github.stefanbirkner:system-rules:1.19.0",
  "com.google.api.grpc:proto-google-common-protos:2.5.1",
  "com.google.code.findbugs:jsr305:3.0.2",
  "com.google.guava:guava-beta-checker:1.0",
  "com.lmax:disruptor:3.4.4",
  "com.sun.net.httpserver:http:20070405",
  "com.tngtech.archunit:archunit-junit5:0.22.0",
  "com.uber.nullaway:nullaway:0.9.2",
  "edu.berkeley.cs.jqf:jqf-fuzz:1.7",
  "eu.rekawek.toxiproxy:toxiproxy-java:2.1.5",
  "io.github.netmikey.logunit:logunit-jul:1.1.0",
  "io.jaegertracing:jaeger-client:1.6.0",
  "io.opentelemetry.proto:opentelemetry-proto:0.9.0-alpha",
  "io.opentracing:opentracing-api:0.33.0",
  "junit:junit:4.13.2",
  "nl.jqno.equalsverifier:equalsverifier:3.8",
  "org.assertj:assertj-core:3.21.0",
  "org.awaitility:awaitility:4.1.1",
  "org.bouncycastle:bcpkix-jdk15on:1.70",
  "org.codehaus.mojo:animal-sniffer-annotations:1.20",
  "org.jctools:jctools-core:3.3.0",
  "org.junit-pioneer:junit-pioneer:1.5.0",
  "org.skyscreamer:jsonassert:1.5.0",
  "org.slf4j:slf4j-simple:1.7.32"
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
        api("${set.group}:$module:${set.version}")
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

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isGuava = version.endsWith("-jre")
  val isStable = stableKeyword || regex.matches(version) || isGuava
  return isStable.not()
}

tasks {
  named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"
    checkConstraints = true

    rejectVersionIf {
      isNonStable(candidate.version)
    }
  }
}
