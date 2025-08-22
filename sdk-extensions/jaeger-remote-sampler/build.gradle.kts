plugins {
  id("otel.protobuf-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry - Jaeger Remote sampler"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.jaeger")

dependencies {
  api(project(":sdk:all"))
  compileOnly(project(":api:incubator"))
  compileOnly(project(":sdk-extensions:autoconfigure"))
  compileOnly(project(":sdk-extensions:incubator"))

  implementation(project(":sdk:all"))
  implementation(project(":exporters:common"))
  implementation(project(":exporters:sender:okhttp"))

  implementation("com.squareup.okhttp3:okhttp-jvm")

  compileOnly("io.grpc:grpc-api")
  compileOnly("io.grpc:grpc-protobuf")
  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk-extensions:autoconfigure"))
  testImplementation("com.google.guava:guava")
  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc-protocol")
  testImplementation("org.testcontainers:junit-jupiter")
}

testing {
  suites {
    register<JvmTestSuite>("testGrpcNetty") {
      dependencies {
        implementation(project(":sdk:testing"))
        implementation(project(":exporters:common"))
        implementation("com.google.protobuf:protobuf-java")
        implementation("com.linecorp.armeria:armeria-junit5")
        implementation("com.linecorp.armeria:armeria-grpc-protocol")
        implementation("org.testcontainers:junit-jupiter")
        implementation("io.grpc:grpc-netty")
        implementation("io.grpc:grpc-stub")
      }
    }
  }
}

afterEvaluate {
  // Classpath when compiling testGrpcNetty, we add dependency management directly
  // since it doesn't follow Gradle conventions of naming / properties.
  dependencies {
    add("testGrpcNettyCompileProtoPath", platform(project(":dependencyManagement")))
  }
}

tasks {
  check {
    dependsOn(testing.suites)
  }
}

wire {
  custom {
    schemaHandlerFactoryClass = "io.opentelemetry.gradle.ProtoFieldsWireHandlerFactory"
  }
}

tasks {
  compileJava {
    with(options) {
      // Generated code so can't control serialization.
      compilerArgs.add("-Xlint:-serial")
    }
  }

  checkstyleMain {
    // overrides the default which includes generated proto sources
    source = fileTree("src/main/java")
  }
}
