# OpenTelemetry - Jaeger Exporter - Thrift

[![Javadocs][javadoc-image]][javadoc-url]

> **NOTICE**: Jaeger now
> has [native support for OTLP](https://opentelemetry.io/blog/2022/jaeger-native-otlp/) and jaeger
> exporters are now deprecated. `opentelemetry-exporter-jaeger-thrift` will continue to be published until
> 1.27.0 (July 2023). After 1.27.0, it will continue to receive patches for security vulnerabilities,
> and `io.opentelemetry:opentelemetry-bom` will reference the last published version.

This is the OpenTelemetry exporter, sending span data to Jaeger via Thrift over HTTP.

## Configuration

The Jaeger Thrift span exporter can be configured programmatically.

An example of simple Jaeger Thrift exporter initialization. In this case
spans will be sent to a Jaeger Thrift endpoint running on `localhost`:

```java
JaegerThriftSpanExporter exporter =
        JaegerThriftSpanExporter.builder()
            .setEndpoint("http://localhost:14268/api/traces")
            .build();
```

If you need configuration via environment variables and/or system properties, you will want to use
the [autoconfigure](../../sdk-extensions/autoconfigure) module.

## Compatibility

As with the OpenTelemetry SDK itself, this exporter is compatible with Java 8+ and Android API level 24+.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-jaeger-thrift.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-jaeger-thrift
