plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry Prometheus Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.prometheus")

testSets {
  create("integrationTest")
}

dependencies {
  api(project(":sdk:metrics"))

  api("io.prometheus:simpleclient")
  implementation("io.prometheus:simpleclient_common")

  compileOnly("com.sun.net.httpserver:http")

  testImplementation("com.google.guava:guava")
  testImplementation("com.linecorp.armeria:armeria")
  testRuntimeOnly("org.slf4j:slf4j-simple")

  add("integrationTestImplementation", "com.fasterxml.jackson.jr:jackson-jr-stree")
  add("integrationTestImplementation", "com.linecorp.armeria:armeria")
  add("integrationTestImplementation", "org.testcontainers:junit-jupiter")
}

tasks {
  check {
    dependsOn("integrationTest")
  }
}
