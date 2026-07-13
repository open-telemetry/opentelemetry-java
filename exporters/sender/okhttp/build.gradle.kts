plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry OkHttp Senders"
otelJava.moduleName.set("io.opentelemetry.exporter.sender.okhttp.internal")
otelJava.osgiServiceLoaderProvides.set(listOf(
  "io.opentelemetry.sdk.common.export.GrpcSenderProvider",
  "io.opentelemetry.sdk.common.export.HttpSenderProvider"
))
// okhttp3, okio, and org.jspecify.annotations are not OSGi bundles; imports must be optional.
// (org.jspecify.annotations is pulled in by OkHttp's Kotlin-compiled types, not this bundle's code.)
otelJava.osgiUnversionedOptionalPackages.set(listOf("okhttp3", "okio", "org.jspecify.annotations"))

dependencies {
  implementation(project(":exporters:common"))
  implementation(project(":sdk:common"))

  annotationProcessor("com.google.auto.value:auto-value")

  implementation("com.squareup.okhttp3:okhttp")

  compileOnly("io.grpc:grpc-stub")

  testImplementation("com.linecorp.armeria:armeria-junit5")
}
