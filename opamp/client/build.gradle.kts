plugins {
  id("otel.java-conventions")
  id("otel.animalsniffer-conventions")
//  id("otel.publish-conventions")
}

description = "OpAMP Client"
otelJava.moduleName.set("io.opentelemetry.opamp-client")
base.archivesName.set("opamp-client")
