plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}

description = "OpenTelemetry JDK HttpSender"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.jdk.internal")

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  compileOnly("com.fasterxml.jackson.core:jackson-core")

  // NOTE: this is a strange dependency. junit reflectively analyzes classes as part of its test discovery process, eventually encounters to jackson-databind classes, and fails with a NoClassDefFoundError:
  // JUnit Jupiter > initializationError FAILED
  //    org.junit.platform.launcher.core.DiscoveryIssueException: TestEngine with ID 'junit-jupiter' encountered a critical issue during test discovery:
  //
  //    (1) [ERROR] ClassSelector [className = 'io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderTest$NoOpMarshaler', classLoader = jdk.internal.loader.ClassLoaders$AppClassLoader@2aae9190] resolution failed
  //        Source: ClassSource [className = 'io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderTest$NoOpMarshaler', filePosition = null]
  //                at io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSenderTest$NoOpMarshaler.<no-method>(SourceFile:0)
  //        Cause: java.lang.NoClassDefFoundError: com/fasterxml/jackson/core/JsonGenerator
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
}

tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.release.set(11)
  }
}

tasks.test {
  val testJavaVersion: String? by project
  enabled = !testJavaVersion.equals("8")
}
