plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Exporter Common"
otelJava.moduleName.set("io.opentelemetry.exporter.internal")

val versions: Map<String, String> by project
dependencies {
  api(project(":api:all"))

  compileOnly(project(":sdk:common"))

  compileOnly("org.codehaus.mojo:animal-sniffer-annotations")

  annotationProcessor("com.google.auto.value:auto-value")

  // We include helpers shared by gRPC or okhttp exporters but do not want to impose these
  // dependency on all of our consumers.
  compileOnly("com.fasterxml.jackson.core:jackson-core")
  compileOnly("com.squareup.okhttp3:okhttp")
  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:common"))

  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("com.squareup.okhttp3:okhttp")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("org.skyscreamer:jsonassert")
  testImplementation("com.google.api.grpc:proto-google-common-protos")
  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation(project(":exporters:otlp:all"))
  jmhImplementation(project(":exporters:otlp:common"))
  jmhImplementation("com.linecorp.armeria:armeria")
  jmhImplementation("com.linecorp.armeria:armeria-grpc")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhRuntimeOnly("com.squareup.okhttp3:okhttp")
  jmhRuntimeOnly("io.grpc:grpc-netty")
}

sourceSets {
  create("java11") {
    java {
      setSrcDirs(listOf("src/main/java11"))
    }
  }
}

configurations {
  named("java11Implementation") {
    extendsFrom(configurations["implementation"])
  }
  named("java11CompileOnly") {
    extendsFrom(configurations["compileOnly"])
  }
}

tasks {
  named<JavaCompile>("compileJava11Java") {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.release.set(11)
  }

  withType(Jar::class) {
    into("META-INF/versions/11") {
      from(sourceSets["java11"].output)
    }
    manifest.attributes(
      "Multi-Release" to "true",
    )
  }
}
