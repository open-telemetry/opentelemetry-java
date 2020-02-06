# OpenTelemetry QuickStart

<!-- Re-generate TOC with `markdown-toc --no-first-h1 -i` -->

<!-- toc -->

<!-- tocstop -->


OpenTelemetry can be used to instrument code for collecting distributed traces and recording metrics.
For more details, check out the project [Overview].

In the following examples, we demonstrate how to create spans and record metrics through the OpenTelemetry API.

[Overview]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md

# Tracing

**Libraries** that want to export distributed tracing using OpenTelemetry must only take dependency on the `opentelemetry-api` package
and should never configure OpenTelemetry. The configuration must be provided by **Applications** which should also depend on the 
`opentelemetry-sdk` package, or any other implementation of the OpenTelemetry API. This way, libraries will obtain a real tracer
implementation only if the user application is instrumented. For more details, check out the [Library Guidelines].

[Library Guidelines]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/library-guidelines.md

## Configuration

Configuration is performed only by user applications which should configure the exporter and may tune the OpenTelemetry behavior.

For example, a basic configuration instantiates the SDK tracer registry and sets to export the traces to a logging file.  

```
// Get the tracer
TracerSdkRegistry tracerRegistry = OpenTelemetrySdk.getTracerRegistry();

// Set to export the traces to a log file
tracerRegistry.addSpanProcessor(SimpleSpansProcessor.newBuilder(new LoggingExporter()).build());
```

### Sampler

It is not always feasible to trace and export every user request in an application.
In order to strike a balance between observability and expenses, traces are sampled. 

The OpenTelemetry SDK offers three samplers out of the box:
 - Always sample
 - Never sample
 - Sample based on probability 

```
TraceConfig AlwaysON = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.alwaysOn()
).build();
TraceConfig AlwaysOff = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.alwaysOff()
).build();
TraceConfig half = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.probability(0.5)
).build();
// Configure the sampler to use
tracerRegistry.updateActiveTraceConfig(
    half
);
```

### Span Processor

Different Span processors are offered by OpenTelemetry. 
The `SimpleSpanProcessor` immediately forwards ended spans to the exporter, while the `BatchSpansProcessor` batches them and send them in bulk. 
Multiple Span processors can be configured to be active at the same time using `MultiSpanProcessor`.

```
tracerRegistry.addSpanProcessor(
    SimpleSpansProcessor.newBuilder(new LoggingExporter()).build()
);
tracerRegistry.addSpanProcessor(
    BatchSpansProcessor.newBuilder(new LoggingExporter()).build()
);
tracerRegistry.addSpanProcessor(MultiSpanProcessor.create(Arrays.asList(
            SimpleSpansProcessor.newBuilder(new LoggingExporter()).build(),
            BatchSpansProcessor.newBuilder(new LoggingExporter()).build()
)));
```

### Exporter

### In Memory exporter

### Jaeger exporter

### Logging exporter

### OpenTelemetry exporter
TODO

## Create basic Span

## Create nested Spans

## Create Spans with events

## Semantic Conventions

## Span with links

## Context Propagation

# Metric