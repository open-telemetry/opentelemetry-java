plugins {
  id("otel.java-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry Integration Tests"
otelJava.moduleName.set("io.opentelemetry.integration.tests")

testSets {
  create("testJaeger")

  libraries {
    create("testOtlpCommon")
  }

  create("testOtlp") {
    imports("testOtlpCommon")
  }

  create("testOtlpNoGrpcJava") {
    imports("testOtlpCommon")
  }
}

dependencies {
  testImplementation(project(":sdk:all"))
  testImplementation(project(":sdk:testing"))
  testImplementation(project(":extensions:trace-propagators"))

  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.slf4j:slf4j-simple")

  add("testOtlpCommonImplementation", project(":exporters:otlp:trace"))
  add("testOtlpCommonImplementation", project(":exporters:otlp:metrics"))
  add("testOtlpCommonImplementation", project(":exporters:otlp:logs"))
  add("testOtlpCommonImplementation", project(":exporters:otlp-http:logs"))
  add("testOtlpCommonImplementation", project(":exporters:otlp-http:metrics"))
  add("testOtlpCommonImplementation", project(":exporters:otlp-http:trace"))
  add("testOtlpCommonImplementation", project(":semconv"))
  add("testOtlpCommonImplementation", project(":proto"))
  add("testOtlpCommonImplementation", "com.linecorp.armeria:armeria-grpc-protocol")
  add("testOtlpCommonImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testOtlpCommonImplementation", "org.assertj:assertj-core")
  add("testOtlpCommonImplementation", "org.awaitility:awaitility")
  add("testOtlpCommonImplementation", "org.junit.jupiter:junit-jupiter-params")
  add("testOtlpCommonImplementation", "org.testcontainers:junit-jupiter")

  add("testOtlpRuntimeOnly", "io.grpc:grpc-netty-shaded")

  add("testJaegerImplementation", project(":exporters:jaeger"))
  add("testJaegerImplementation", project(":semconv"))
  add("testJaegerRuntimeOnly", "io.grpc:grpc-netty-shaded")
}

tasks {
  check {
    dependsOn("testJaeger", "testOtlp", "testOtlpNoGrpcJava")
  }
}

configurations {
  named("testOtlpNoGrpcJavaRuntimeClasspath") {
    dependencies {
      exclude("io.grpc")
    }
  }
}
