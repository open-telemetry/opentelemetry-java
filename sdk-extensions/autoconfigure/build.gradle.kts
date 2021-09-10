plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry SDK Auto-configuration"
otelJava.moduleName.set("io.opentelemetry.sdk.autoconfigure")

testSets {
  create("testConfigError")
  create("testFullConfig")
  create("testInitializeRegistersGlobal")
  create("testJaeger")
  create("testOtlpGrpc")
  create("testOtlpHttp")
  create("testPrometheus")
  create("testResourceDisabledByProperty")
  create("testResourceDisabledByEnv")
  create("testZipkin")
}

dependencies {
  api(project(":sdk:opentelemetry-sdk"))
  api(project(":sdk:opentelemetry-sdk-metrics"))
  api(project(":sdk-extensions:opentelemetry-sdk-extension-autoconfigure-spi"))

  implementation(project(":opentelemetry-semconv"))

  compileOnly(project(":exporters:opentelemetry-exporter-jaeger"))
  compileOnly(project(":exporters:opentelemetry-exporter-logging"))
  compileOnly(project(":exporters:otlp:opentelemetry-exporter-otlp"))
  compileOnly(project(":exporters:otlp:opentelemetry-exporter-otlp-metrics"))
  compileOnly(project(":exporters:otlp-http:opentelemetry-exporter-otlp-http-trace"))
  compileOnly(project(":exporters:otlp-http:opentelemetry-exporter-otlp-http-metrics"))
  compileOnly(project(":exporters:opentelemetry-exporter-prometheus"))
  compileOnly("io.prometheus:simpleclient_httpserver")
  compileOnly(project(":exporters:opentelemetry-exporter-zipkin"))

  testImplementation(project(path = ":sdk:sdk-trace-shaded-deps"))

  testImplementation(project(":opentelemetry-proto"))
  testImplementation(project(":sdk:opentelemetry-sdk-testing"))
  testImplementation("com.linecorp.armeria:armeria-junit5")
  testImplementation("com.linecorp.armeria:armeria-grpc")
  testRuntimeOnly("io.grpc:grpc-netty-shaded")
  testRuntimeOnly("org.slf4j:slf4j-simple")

  add("testFullConfigImplementation", project(":extensions:opentelemetry-extension-aws"))
  add("testFullConfigImplementation", project(":extensions:opentelemetry-extension-trace-propagators"))
  add("testFullConfigImplementation", project(":exporters:opentelemetry-exporter-jaeger"))
  add("testFullConfigImplementation", project(":exporters:opentelemetry-exporter-logging"))
  add("testFullConfigImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp"))
  add("testFullConfigImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp-metrics"))
  add("testFullConfigImplementation", project(":exporters:opentelemetry-exporter-prometheus"))
  add("testFullConfigImplementation", "io.prometheus:simpleclient_httpserver")
  add("testFullConfigImplementation", project(":exporters:opentelemetry-exporter-zipkin"))
  add("testFullConfigImplementation", project(":sdk-extensions:opentelemetry-sdk-extension-resources"))

  add("testOtlpGrpcImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp"))
  add("testOtlpGrpcImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp-metrics"))
  add("testOtlpGrpcImplementation", "org.bouncycastle:bcpkix-jdk15on")
  add("testOtlpHttpImplementation", project(":exporters:otlp-http:opentelemetry-exporter-otlp-http-trace"))
  add("testOtlpHttpImplementation", project(":exporters:otlp-http:opentelemetry-exporter-otlp-http-metrics"))
  add("testOtlpHttpImplementation", "com.squareup.okhttp3:okhttp")
  add("testOtlpHttpImplementation", "com.squareup.okhttp3:okhttp-tls")
  add("testOtlpHttpImplementation", "org.bouncycastle:bcpkix-jdk15on")

  add("testJaegerImplementation", project(":exporters:opentelemetry-exporter-jaeger"))

  add("testZipkinImplementation", project(":exporters:opentelemetry-exporter-zipkin"))

  add("testConfigErrorImplementation", project(":extensions:opentelemetry-extension-trace-propagators"))
  add("testConfigErrorImplementation", project(":exporters:opentelemetry-exporter-jaeger"))
  add("testConfigErrorImplementation", project(":exporters:opentelemetry-exporter-logging"))
  add("testConfigErrorImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp"))
  add("testConfigErrorImplementation", project(":exporters:otlp:opentelemetry-exporter-otlp-metrics"))
  add("testConfigErrorImplementation", project(":exporters:opentelemetry-exporter-prometheus"))
  add("testConfigErrorImplementation", "io.prometheus:simpleclient_httpserver")
  add("testConfigErrorImplementation", project(":exporters:opentelemetry-exporter-zipkin"))
  add("testConfigErrorImplementation", "org.junit-pioneer:junit-pioneer")

  add("testPrometheusImplementation", project(":exporters:opentelemetry-exporter-prometheus"))
  add("testPrometheusImplementation", "io.prometheus:simpleclient_httpserver")

  add("testResourceDisabledByPropertyImplementation", project(":sdk-extensions:opentelemetry-sdk-extension-resources"))
  add("testResourceDisabledByEnvImplementation", project(":sdk-extensions:opentelemetry-sdk-extension-resources"))
}

tasks {
  val testConfigError by existing(Test::class)

  val testFullConfig by existing(Test::class) {
    environment("OTEL_METRICS_EXPORTER", "otlp")
    environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=test,cat=meow")
    environment("OTEL_PROPAGATORS", "tracecontext,baggage,b3,b3multi,jaeger,ottrace,xray,test")
    environment("OTEL_BSP_SCHEDULE_DELAY", "10")
    environment("OTEL_IMR_EXPORT_INTERVAL", "10")
    environment("OTEL_EXPORTER_OTLP_HEADERS", "cat=meow,dog=bark")
    environment("OTEL_EXPORTER_OTLP_TIMEOUT", "5000")
    environment("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT", "2")
    environment("OTEL_TEST_CONFIGURED", "true")
  }

  val testInitializeRegistersGlobal by existing(Test::class) {
    environment("OTEL_TRACES_EXPORTER", "none")
  }

  val testJaeger by existing(Test::class) {
    environment("OTEL_TRACES_EXPORTER", "jaeger")
    environment("OTEL_BSP_SCHEDULE_DELAY", "10")
  }

  val testOtlpGrpc by existing(Test::class) {
    environment("OTEL_METRICS_EXPORTER", "otlp")
  }

  val testOtlpHttp by existing(Test::class) {
    environment("OTEL_METRICS_EXPORTER", "otlp")
  }

  val testZipkin by existing(Test::class) {
    environment("OTEL_TRACES_EXPORTER", "zipkin")
    environment("OTEL_BSP_SCHEDULE_DELAY", "10")
  }

  val testPrometheus by existing(Test::class) {
    environment("OTEL_TRACES_EXPORTER", "none")
    environment("OTEL_METRICS_EXPORTER", "prometheus")
    environment("OTEL_IMR_EXPORT_INTERVAL", "10")
  }

  val testResourceDisabledByProperty by existing(Test::class) {
    jvmArgs("-Dotel.java.disabled.resource-providers=io.opentelemetry.sdk.extension.resources.OsResourceProvider,io.opentelemetry.sdk.extension.resources.ProcessResourceProvider")
    // Properties win, this is ignored.
    environment("OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS", "io.opentelemetry.sdk.extension.resources.ProcessRuntimeResourceProvider")
    environment("OTEL_TRACES_EXPORTER", "none")
    environment("OTEL_METRICS_EXPORTER", "none")
  }

  val testResourceDisabledByEnv by existing(Test::class) {
    environment("OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS", "io.opentelemetry.sdk.extension.resources.OsResourceProvider,io.opentelemetry.sdk.extension.resources.ProcessResourceProvider")
    environment("OTEL_TRACES_EXPORTER", "none")
    environment("OTEL_METRICS_EXPORTER", "none")
  }

  check {
    dependsOn(
      testConfigError,
      testFullConfig,
      testInitializeRegistersGlobal,
      testJaeger,
      testOtlpGrpc,
      testOtlpHttp,
      testPrometheus,
      testZipkin,
      testResourceDisabledByProperty,
      testResourceDisabledByEnv
    )
  }
}
