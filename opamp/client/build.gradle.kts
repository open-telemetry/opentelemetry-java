plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpAMP Client"
otelJava.moduleName.set("io.opentelemetry.opamp-client")
base.archivesName.set("opamp-client")
