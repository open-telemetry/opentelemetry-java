plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("org.unbroken-dome.test-sets")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Protocol Trace Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.trace")

testSets {
  create("testGrpcNetty")
  create("testGrpcNettyShaded")
  create("testGrpcOkhttp")
  create("testGrpcNetty138")
}

dependencies {
  api(project(":sdk:trace"))

  implementation(project(":api:metrics"))
  implementation(project(":exporters:otlp:common"))

  compileOnly("io.grpc:grpc-netty")
  compileOnly("io.grpc:grpc-netty-shaded")
  compileOnly("io.grpc:grpc-okhttp")

  api("io.grpc:grpc-stub")
  implementation("io.grpc:grpc-api")

  testImplementation(project(":sdk:testing"))

  testImplementation("io.grpc:grpc-protobuf")
  testImplementation("io.grpc:grpc-testing")
  testImplementation("org.slf4j:slf4j-simple")

  testImplementation("org.jeasy:easy-random-randomizers")

  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyRuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNettyRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcNetty138Implementation", enforcedPlatform("io.grpc:grpc-bom:1.38.0"))
  add("testGrpcNetty138Implementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNetty138Implementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNetty138RuntimeOnly", "io.grpc:grpc-netty")
  add("testGrpcNetty138RuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcNettyShadedImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcNettyShadedRuntimeOnly", "io.grpc:grpc-netty-shaded")
  add("testGrpcNettyShadedRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-grpc")
  add("testGrpcOkhttpImplementation", "com.linecorp.armeria:armeria-junit5")
  add("testGrpcOkhttpRuntimeOnly", "io.grpc:grpc-okhttp")
  add("testGrpcOkhttpRuntimeOnly", "org.bouncycastle:bcpkix-jdk15on")

  jmh(project(":sdk:testing"))
  jmh("io.grpc:grpc-netty")
}

tasks {
  named("check") {
    dependsOn("testGrpcNetty", "testGrpcNetty138", "testGrpcNettyShaded", "testGrpcOkhttp")
  }
}
