[![Javadocs][javadoc-image]][javadoc-url]
# OpenTelemetry - OpenTracing Shim
The OpenTracing shim is a bridge layer from OpenTelemetry to the OpenTracing API.
It takes OpenTelemetry Tracer and exposes it as an implementation of an OpenTracing Tracer.

## Usage

There are 2 ways to expose an OpenTracing tracer: 
1. From the global OpenTelemetry configuration: 
    ```java
    Tracer tracer = OpenTracingShim.createTracerShim();
    ```
1. From a provided `OpenTelemetry` instance:
    ```java
    Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
    ```

Optionally register the tracer as the OpenTracing GlobalTracer:
```java
GlobalTracer.registerIfAbsent(tracer);
```

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opentracing-shim.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opentracing-shim
