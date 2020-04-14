# OpenTelemetry - Zipkin Span Exporter

[![Javadocs][javadoc-image]][javadoc-url]

This is the OpenTelemetry exporter, which sends span data to Zipkin.

## Configuration

The Zipkin span exporter can be configured programmatically.

An example of simple Zipkin exporter initialization. In this case
spans will be sent to a Zipkin endpoint running on `localhost`:

```java
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder()
            .setV2Url("http://localhost/api/v2/spans")
            .setServiceName("my-service")
            .build();

    ZipkinSpanExporter exporter = ZipkinSpanExporter.create(configuration);
```

## Attribution

The code in this module is based on the [OpenCensus Zipkin exporter][oc-origin] code.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporters-zipkin.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporters-zipkin
[oc-origin]: https://github.com/census-instrumentation/opencensus-java/
