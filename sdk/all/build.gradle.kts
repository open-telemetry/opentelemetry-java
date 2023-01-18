plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry SDK"
otelJava.moduleName.set("io.opentelemetry.sdk")
base.archivesName.set("opentelemetry-sdk")

dependencies {
  api(project(":api:all"))
  api(project(":sdk:common"))
  api(project(":sdk:trace"))
  api(project(":sdk:metrics"))

  // implementation dependency to require users to add the artifact directly to their build to use
  // SdkLoggerProvider.
  implementation(project(":sdk:logs"))

  annotationProcessor("com.google.auto.value:auto-value")

  testAnnotationProcessor("com.google.auto.value:auto-value")

  testImplementation(project(":sdk:testing"))
}
