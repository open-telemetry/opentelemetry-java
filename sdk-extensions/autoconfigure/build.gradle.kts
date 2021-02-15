plugins {
    `java-library`
    `maven-publish`

    id("org.unbroken-dome.test-sets")
}

description = "OpenTelemetry SDK Auto-configuration"
extra["moduleName"] = "io.opentelemetry.sdk.autoconfigure"

testSets {
    create("testConfigError")
    create("testFullConfig")
    create("testInitializeRegistersGlobal")
    create("testJaeger")
    create("testPrometheus")
    create("testOtlpTls")
    create("testZipkin")
}

dependencies {
    api(project(":sdk:all"))
    api(project(":sdk:metrics"))

    compileOnly(project(":extensions:trace-propagators"))
    compileOnly(project(":exporters:jaeger"))
    compileOnly(project(":exporters:logging"))
    compileOnly(project(":exporters:otlp:all"))
    compileOnly(project(":exporters:otlp:metrics"))
    compileOnly(project(":exporters:prometheus"))
    compileOnly("io.prometheus:simpleclient_httpserver")
    compileOnly(project(":exporters:zipkin"))

    testImplementation(project(":proto"))
    testImplementation(project(":sdk:testing"))
    testImplementation("com.linecorp.armeria:armeria-junit5")
    testImplementation("com.linecorp.armeria:armeria-grpc")
    testRuntimeOnly("io.grpc:grpc-netty-shaded")
    testRuntimeOnly("org.slf4j:slf4j-simple")

    add("testFullConfigImplementation", project(":extensions:aws"))
    add("testFullConfigImplementation", project(":extensions:trace-propagators"))
    add("testFullConfigImplementation", project(":exporters:jaeger"))
    add("testFullConfigImplementation", project(":exporters:logging"))
    add("testFullConfigImplementation", project(":exporters:otlp:all"))
    add("testFullConfigImplementation", project(":exporters:otlp:metrics"))
    add("testFullConfigImplementation", project(":exporters:prometheus"))
    add("testFullConfigImplementation", "io.prometheus:simpleclient_httpserver")
    add("testFullConfigImplementation", project(":exporters:zipkin"))

    add("testOtlpTlsImplementation", project(":exporters:otlp:all"))

    add("testJaegerImplementation", project(":exporters:jaeger"))

    add("testZipkinImplementation", project(":exporters:zipkin"))

    add("testConfigErrorImplementation", project(":extensions:trace-propagators"))
    add("testConfigErrorImplementation", project(":exporters:jaeger"))
    add("testConfigErrorImplementation", project(":exporters:logging"))
    add("testConfigErrorImplementation", project(":exporters:otlp:all"))
    add("testConfigErrorImplementation", project(":exporters:otlp:metrics"))
    add("testConfigErrorImplementation", project(":exporters:prometheus"))
    add("testConfigErrorImplementation", "io.prometheus:simpleclient_httpserver")
    add("testConfigErrorImplementation", project(":exporters:zipkin"))
    add("testConfigErrorImplementation", "org.junit-pioneer:junit-pioneer")

    add("testPrometheusImplementation", project(":exporters:prometheus"))
    add("testPrometheusImplementation", "io.prometheus:simpleclient_httpserver")
}

tasks {
    val testConfigError by existing(Test::class) {
    }

    val testFullConfig by existing(Test::class) {
        environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=test,cat=meow")
        environment("OTEL_TRACES_EXPORTER", "otlp")
        environment("OTEL_METRICS_EXPORTER", "otlp")
        environment("OTEL_PROPAGATORS", "tracecontext,baggage,b3,b3multi,jaeger,ottrace,xray,test")
        environment("OTEL_BSP_SCHEDULE_DELAY", "10")
        environment("OTEL_IMR_EXPORT_INTERVAL", "10")
        environment("OTEL_EXPORTER_OTLP_HEADERS", "cat=meow,dog=bark")
        environment("OTEL_EXPORTER_OTLP_TIMEOUT", "5000")
        environment("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT", "2")
    }

    val testJaeger by existing(Test::class) {
        environment("OTEL_TRACES_EXPORTER", "jaeger")
        environment("OTEL_BSP_SCHEDULE_DELAY", "10")
    }

    val testOtlpTls by existing(Test::class) {
        environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=test,cat=meow")
        environment("OTEL_TRACES_EXPORTER", "otlp")
        environment("OTEL_BSP_SCHEDULE_DELAY", "10")
    }

    val testZipkin by existing(Test::class) {
        environment("OTEL_TRACES_EXPORTER", "zipkin")
        environment("OTEL_BSP_SCHEDULE_DELAY", "10")
    }

    val testPrometheus by existing(Test::class) {
        environment("OTEL_METRICS_EXPORTER", "prometheus")
        environment("OTEL_IMR_EXPORT_INTERVAL", "10")
    }

    val check by existing {
        dependsOn(testConfigError, testFullConfig, testJaeger, testPrometheus, testZipkin)
    }
}
