import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  `java-platform`

  id("com.github.ben-manes.versions")
}

data class DependencySet(val group: String, val version: String, val modules: List<String>)

val dependencyVersions = hashMapOf<String, String>()
rootProject.extra["versions"] = dependencyVersions

val DEPENDENCY_BOMS = listOf(
  "com.fasterxml.jackson:jackson-bom:2.14.0",
  "com.google.guava:guava-bom:31.1-jre",
  "com.google.protobuf:protobuf-bom:3.21.9",
  "com.linecorp.armeria:armeria-bom:1.20.2",
  "com.squareup.okhttp3:okhttp-bom:4.10.0",
  "io.grpc:grpc-bom:1.50.2",
  "io.zipkin.brave:brave-bom:5.14.1",
  "io.zipkin.reporter2:zipkin-reporter-bom:2.16.3",
  "org.junit:junit-bom:5.9.1",
  "org.testcontainers:testcontainers-bom:1.17.5",
  "org.yaml:snakeyaml:1.33"
)

val DEPENDENCY_SETS = listOf(
  DependencySet(
    "com.google.auto.value",
    "1.9",
    listOf("auto-value", "auto-value-annotations")
  ),
  DependencySet(
    "com.google.errorprone",
    "2.15.0",
    listOf("error_prone_annotations", "error_prone_core")
  ),
  DependencySet(
    "io.opencensus",
    "0.31.1",
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
    "0.16.0",
    listOf("simpleclient", "simpleclient_common", "simpleclient_httpserver")
  ),
  DependencySet(
    "javax.annotation",
    "1.3.2",
    listOf("javax.annotation-api")
  ),
  DependencySet(
    "org.openjdk.jmh",
    "1.35",
    listOf("jmh-core", "jmh-generator-bytecode")
  ),
  DependencySet(
    "org.mockito",
    "4.7.0",
    listOf("mockito-core", "mockito-junit-jupiter")
  ),
  DependencySet(
    "org.slf4j",
    "2.0.0",
    listOf("jul-to-slf4j", "slf4j-simple")
  ),
)

val DEPENDENCIES = listOf(
  "com.github.stefanbirkner:system-rules:1.19.0",
  "com.google.api.grpc:proto-google-common-protos:2.10.0",
  "com.google.code.findbugs:jsr305:3.0.2",
  "com.google.guava:guava-beta-checker:1.0",
  "com.lmax:disruptor:3.4.4",
  "com.sun.net.httpserver:http:20070405",
  "com.tngtech.archunit:archunit-junit5:1.0.0",
  "com.uber.nullaway:nullaway:0.10.4",
  // TODO(anuraaga): Skip 1.8 because of https://github.com/rohanpadhye/JQF/issues/172
  "edu.berkeley.cs.jqf:jqf-fuzz:1.7",
  "eu.rekawek.toxiproxy:toxiproxy-java:2.1.7",
  "io.github.netmikey.logunit:logunit-jul:1.1.3",
  "io.jaegertracing:jaeger-client:1.8.1",
  "io.opentelemetry.proto:opentelemetry-proto:0.19.0-alpha",
  "io.opentracing:opentracing-api:0.33.0",
  "junit:junit:4.13.2",
  "nl.jqno.equalsverifier:equalsverifier:3.11",
  "org.assertj:assertj-core:3.23.1",
  "org.awaitility:awaitility:4.2.0",
  "org.bouncycastle:bcpkix-jdk15on:1.70",
  "org.codehaus.mojo:animal-sniffer-annotations:1.22",
  "org.jctools:jctools-core:4.0.1",
  "org.junit-pioneer:junit-pioneer:1.8.0",
  "org.skyscreamer:jsonassert:1.5.1",
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
