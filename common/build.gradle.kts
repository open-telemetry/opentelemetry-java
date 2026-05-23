plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry API Common"
otelJava.moduleName.set("io.opentelemetry.common")
// ServiceLoaderComponentLoader (this bundle) is the ServiceLoader.load() call site for all
// ComponentLoader.forClassLoader() usage; requires the processor extender to weave it.
otelJava.osgiServiceLoaderProcessor.set(true)

dependencies {
}
