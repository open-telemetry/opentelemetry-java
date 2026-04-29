plugins {
  id("otel.java-conventions")
  // TODO decide what to do about JfrExportExample before publishing.
  // unlike the other classes here it's an example, not a reusable utility.
  //  id("otel.publish-conventions")
}

description = "OpenTelemetry - JFR to Profiles data conversion example"
otelJava.moduleName.set("io.opentelemetry.jfr.profiles.shim")

tasks {
  // this module uses the jdk.jfr.consumer API, which was backported into 1.8 but is '@since 9'
  // and therefore a bit of a pain to get gradle to compile against...
  compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(null as Int?)
  }
  compileTestJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(null as Int?)
  }
}

dependencies {
  api(project(":exporters:otlp:profiles"))
  api(project(":sdk:profiles"))
}
