plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry Protocol Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.trace")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
  create("testOkHttpOnly")
}

dependencies {
  api(project(":sdk:logs"))

  implementation(project(":exporters:otlp:common"))

  compileOnly("io.grpc:grpc-stub")

  testImplementation(project(":sdk:testing"))

  testImplementation("com.google.protobuf:protobuf-java")
  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
  testImplementation("org.slf4j:slf4j-simple")

  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNettyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")
  add("testGrpcNettyShadedRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")
  add("testGrpcOkhttpRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testOkHttpOnlyImplementation", "com.linecorp.armeria:armeria-grpc-protocol")
  add("testOkHttpOnlyImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testOkHttpOnlyImplementation", "com.squareup.okhttp3:okhttp-tls")
  add("testOkHttpOnlyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")
}

tasks {
  named("check") {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp", "testOkHttpOnly")
  }
}

configurations {
  named("testOkHttpOnlyRuntimeClasspath") {
    dependencies {
      exclude("io.grpc")
    }
  }
}
