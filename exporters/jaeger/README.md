# OpenTelemetry - Jaeger Exporter - gRPC

[![Javadocs][javadoc-image]][javadoc-url]

This is the OpenTelemetry exporter, sending span data to Jaeger via gRPC. 

## Configuration

Jaeger exporter can be configured by system properties and environmental variables.
These configuration properties work only for `Jaeger.GrpcSpanExporter.installDefault()`.

* `JAEGER_ENDPOINT` - agents's gRPC endpoint e.g. `localhost:14250`
* `JAEGER_SERVICE_NAME` - service name e.g. `my-deployment`

An example of simples Jaeger gRPC exporter initialization. In this case
spans will be sent to Jaeger agent running on `localhost`:
```java
Builder builder = JaegerGrpcSpanExporter.Builder.fromEnv();
builder.install(OpenTelemetrySdk.getTracerProvider());
```

## Proto files

The proto files in this repository were copied over from the [Jaeger main repository][proto-origin]. At this moment, they have to be manually synchronize, but a [discussion exists][proto-discussion] on how to properly consume them in a more appropriate manner.

[proto-origin]: https://github.com/jaegertracing/jaeger/tree/5b8c1f40f932897b9322bf3f110d830536ae4c71/model/proto
[proto-discussion]: https://github.com/open-telemetry/opentelemetry-java/issues/235
[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporters-jaeger.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporters-jaeger
