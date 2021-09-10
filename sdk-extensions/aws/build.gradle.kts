plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
}

description = "OpenTelemetry SDK AWS Instrumentation Support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.trace.aws")

dependencies {
  api(project(":api:opentelemetry-api"))
  api(project(":sdk:opentelemetry-sdk"))

  compileOnly(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure"))

  annotationProcessor("com.google.auto.value:auto-value")

  implementation(project(":opentelemetry-semconv"))

  implementation("com.fasterxml.jackson.core:jackson-core")
  implementation("com.fasterxml.jackson.core:jackson-databind")

  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure"))

  testImplementation("com.linecorp.armeria:armeria-junit5")
  testRuntimeOnly("org.bouncycastle:bcpkix-jdk15on")

  testImplementation("com.google.guava:guava")
  testImplementation("org.slf4j:slf4j-simple")
  testImplementation("org.skyscreamer:jsonassert")
}
