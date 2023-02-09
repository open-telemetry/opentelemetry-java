# OpenTelemetry - Jaeger Exporter - gRPC

[![Javadocs][javadoc-image]][javadoc-url]

> **NOTICE**: Jaeger now
> has [native support for OTLP](https://opentelemetry.io/blog/2022/jaeger-native-otlp/) and jaeger
> exporters are now deprecated. `opentelemetry-exporter-jaeger` will continue to be published until
> 1.27.0 (July 2023). After 1.27.0, it will continue to receive patches for security vulnerabilities,
> and `io.opentelemetry:opentelemetry-bom` will reference the last published version.

This is the OpenTelemetry exporter, sending span data to Jaeger via gRPC.

## Configuration

The Jaeger gRPC span exporter can be configured programmatically.

An example of simple Jaeger gRPC exporter initialization. In this case
spans will be sent to a Jaeger gRPC endpoint running on `localhost`:

```java
JaegerGrpcSpanExporter exporter =
        JaegerGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:14250")
            .build();
```

If you need configuration via environment variables and/or system properties, you will want to use
the [autoconfigure](../../sdk-extensions/autoconfigure) module.

## Compatibility

As with the OpenTelemetry SDK itself, this exporter is compatible with Java 8+ and Android API level 24+.

## Proto files

The proto files in this repository were copied over from the [Jaeger main repository][proto-origin].
At this moment, they have to be manually synchronized, but a [discussion exists][proto-discussion] on how to properly consume them in a more appropriate manner.

[proto-origin]: https://github.com/jaegertracing/jaeger/tree/5b8c1f40f932897b9322bf3f110d830536ae4c71/model/proto
[proto-discussion]: https://github.com/open-telemetry/opentelemetry-java/issues/235
[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-jaeger.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-jaeger
