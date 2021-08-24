plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("org.unbroken-dome.test-sets")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Metrics Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.metrics")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
}

dependencies {
  api(project(":sdk:metrics"))

  implementation(project(":exporters:otlp:common"))
  implementation(project(":proto"))

  api("io.grpc:grpc-stub")
  implementation("io.grpc:grpc-api")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("com.google.protobuf:protobuf-java")

  testImplementation(project(":sdk:testing"))

  testImplementation("io.grpc:grpc-testing")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")

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
}

tasks {
  named("check") {
    dependsOn("testGrpcNetty", "testGrpcNettyShaded", "testGrpcOkhttp")
  }
}
