# OpenTelemetry - Zipkin Span Exporter

[![Javadocs][javadoc-image]][javadoc-url]

This is an OpenTelemetry exporter that sends span data using the [io.zipkin.reporter2:zipkin-reporter](https://github.com/openzipkin/zipkin-reporter-java) library.

By default, this POSTs json in [Zipkin format](https://zipkin.io/zipkin-api/#/default/post_spans) to
a specified HTTP URL. This could be to a [Zipkin](https://zipkin.io) service, or anything that
consumes the same format.

You can alternatively use other formats, such as protobuf, or override the `Sender` to use a non-HTTP transport, such as Kafka.

## Configuration

The Zipkin span exporter can be configured programmatically.

An example of simple Zipkin exporter initialization. In this case
spans will be sent to a Zipkin endpoint running on `localhost`:

```java
ZipkinSpanExporter exporter =
        ZipkinSpanExporter.builder()
            .setEndpoint("http://localhost/api/v2/spans")
            .build();
```

## Attribution

The code in this module is based on the [OpenCensus Zipkin exporter][oc-origin] code.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-zipkin.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-zipkin
[oc-origin]: https://github.com/census-instrumentation/opencensus-java/
