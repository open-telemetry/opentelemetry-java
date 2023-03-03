# OpenTelemetry - OpenTracing Shim

The OpenTracing shim is a bridge layer from OpenTelemetry to the OpenTracing API.
It takes OpenTelemetry Tracer and exposes it as an implementation of an OpenTracing Tracer.

## Usage

There are 2 ways to expose an OpenTracing tracer:
1. From a provided `OpenTelemetry` instance:
    ```java
    Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
    ```
2. From a specific `TracerProvider`, text map propagator (`TextMapPropagator`), and http propagator (`TextMapPropagator`):
    ```java
    Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry, textMapPropagator, httpPropagator);
    ```

Optionally register the tracer as the OpenTracing GlobalTracer:
```java
GlobalTracer.registerIfAbsent(tracer);
```
