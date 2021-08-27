# OpenTelemetry - Jaeger Exporter - Thrift

[![Javadocs][javadoc-image]][javadoc-url]

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
