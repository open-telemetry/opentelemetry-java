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

tasks.withType<Test>().configureEach {
  // OkHttpHttpSenderTlsCompatibilityTest needs TLSv1/TLSv1.1 available to test COMPATIBLE_TLS
  // against a legacy-protocol-only server. jdk.tls.disabledAlgorithms is a security property
  // (not a regular system property) that the JDK caches the first time any TLS code runs in
  // the JVM, so mutating it at test runtime only works if nothing else in the shared test JVM
  // has touched TLS yet -- not guaranteed. Overriding it via java.security.properties applies
  // at JVM bootstrap, before that caching can happen, so it's always in effect.
  systemProperty(
    "java.security.properties",
    file("src/test/resources/enable-legacy-tls-test.security").absolutePath
  )
}
