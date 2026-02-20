plugins {
  id("otel.java-conventions")
  // TODO: enable to publish
  // id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp4 Senders"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.okhttp.internal")

// Source files are generated from the okhttp (v5) module by replacing the package name.
// Do not edit files under src/ directly - edit the okhttp module instead.
val okhttpDir = project(":exporters:sender:okhttp").projectDir

val generateMainSources by tasks.registering(Copy::class) {
  from(okhttpDir.resolve("src/main/java"))
  into(layout.buildDirectory.dir("generated/sources/okhttp4/main/java"))
  filter { line: String -> line.replace("sender.okhttp.internal", "sender.okhttp4.internal") }
  eachFile { path = path.replace("sender/okhttp/internal", "sender/okhttp4/internal") }
  includeEmptyDirs = false
}

val generateTestSources by tasks.registering(Copy::class) {
  from(okhttpDir.resolve("src/test/java"))
  into(layout.buildDirectory.dir("generated/sources/okhttp4/test/java"))
  filter { line: String -> line.replace("sender.okhttp.internal", "sender.okhttp4.internal") }
  eachFile { path = path.replace("sender/okhttp/internal", "sender/okhttp4/internal") }
  includeEmptyDirs = false
}

val generateMainResources by tasks.registering(Copy::class) {
  from(okhttpDir.resolve("src/main/resources"))
  into(layout.buildDirectory.dir("generated/sources/okhttp4/main/resources"))
  filter { line: String -> line.replace("sender.okhttp.internal", "sender.okhttp4.internal") }
  includeEmptyDirs = false
}

sourceSets {
  main {
    java.srcDir(generateMainSources)
    resources.srcDir(generateMainResources)
  }
  test {
    java.srcDir(generateTestSources)
  }
}

tasks.named<Test>("test") {
  systemProperty("expectedOkHttpMajorVersion", "4")
}

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  compileOnly("io.grpc:grpc-stub")
  compileOnly("com.fasterxml.jackson.core:jackson-core")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
