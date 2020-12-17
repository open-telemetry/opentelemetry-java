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
            .setServiceName("my-service")
            .build();
```

Service name and Endpoint can be also configured via environment variables or system properties.

```java
// Using environment variables
JaegerThriftSpanExporter exporter = 
        JaegerThriftSpanExporter.builder()
            .readEnvironmentVariables()
            .build()
```

```java
// Using system properties
JaegerThriftSpanExporter exporter = 
        JaegerThriftSpanExporter.builder()
            .readSystemProperties()
            .build()
```

The Jaeger Thrift span exporter will look for the following environment variables / system properties:
* `OTEL_EXPORTER_JAEGER_SERVICE_NAME` / `otel.exporter.jaeger.service.name`
* `OTEL_EXPORTER_JAEGER_ENDPOINT` / `otel.exporter.jaeger.endpoint`

## Compatibility

As with the OpenTelemetry SDK itself, this exporter is compatible with Java 8+ and Android API level 24+.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporters-jaeger-thrift.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporters-jaeger-thrift
