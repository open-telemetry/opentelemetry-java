plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Contrib Logging Support"
otelJava.moduleName.set("io.opentelemetry.sdk.extension.logging")

dependencies {
  api(project(":sdk:common"))

  compileOnly("org.osgi:osgi.annotation")

  testImplementation(project(":sdk:logs-testing"))

  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
