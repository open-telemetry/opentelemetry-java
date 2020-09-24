[![Javadocs][javadoc-image]][javadoc-url]
# OpenTelemetry - OpenTracing Shim
A bridge layer from OpenTelemetry to the OpenTracing API.
It takes OpenTelemetry Tracer and expose it as an OpenTracing Tracer.

## Usage

1. Configure OpenTelemetry tracer
1. Create OpenTracing tracer from OpenTelemetry tracer
    ```java
    Tracer tracer = TraceShim.createTracerShim();
    ```
1. Optionally register it as the OpenTracing GlobalTracer
    ```java
    GlobalTracer.registerIfAbsent(tracer);
    ```


[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opentracing-shim.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opentracing-shim