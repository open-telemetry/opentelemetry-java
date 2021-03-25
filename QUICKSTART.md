# OpenTelemetry QuickStart

<!-- Re-generate TOC with `markdown-toc --no-first-h1 -i` -->

<!-- toc -->
- [Set up](#set-up)
- [Tracing](#tracing)
  * [Create basic Span](#create-a-basic-span)
  * [Create nested Spans](#create-nested-spans)
  * [Span Attributes](#span-attributes)
  * [Create Spans with events](#create-spans-with-events)
  * [Create Spans with links](#create-spans-with-links)
  * [Context Propagation](#context-propagation)
- [Metrics](#metrics-alpha-only)
- [Tracing SDK Configuration](#tracing-sdk-configuration)
  * [Sampler](#sampler)
  * [Span Processor](#span-processor)
  * [Exporter](#exporter)
- [Auto Configuration](#auto-configuration)
- [Logging And Error Handling](#logging-and-error-handling)
  * [Examples](#examples)
<!-- tocstop -->

OpenTelemetry can be used to instrument code for collecting telemetry data. For more details, check
out the [OpenTelemetry Website].

**Libraries** that want to export telemetry data using OpenTelemetry MUST only depend on the
`opentelemetry-api` package and should never configure or depend on the OpenTelemetry SDK. The SDK
configuration must be provided by **Applications** which should also depend on the
`opentelemetry-sdk` package, or any other implementation of the OpenTelemetry API. This way,
libraries will obtain a real implementation only if the user application is configured for it. For
more details, check out the [Library Guidelines]. 

## Set up

The first step is to get a handle to an instance of the `OpenTelemetry` interface. 

If you are an application developer, you need to configure an instance of the `OpenTelemetrySdk` as
early as possible in your application. This can be done using the `OpenTelemetrySdk.builder()` method.

For example:

```java
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
        .build();

    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal();
```

As an aside, if you are writing library instrumentation, it is strongly recommended that you provide your users
the ability to inject an instance of `OpenTelemetry` into your instrumentation code. If this is
not possible for some reason, you can fall back to using an instance from the `GlobalOpenTelemetry`
class. Note that you can't force end-users to configure the global, so this is the most brittle
option for library instrumentation.

## Tracing

In the following, we present how to trace code using the OpenTelemetry API. **Note:** Methods of the
OpenTelemetry SDK should never be called.
 
First, a `Tracer` must be acquired, which is responsible for creating spans and interacting with the
[Context](#context-propagation). A tracer is acquired by using the OpenTelemetry API specifying the
name and version of the [library instrumenting][Instrumentation Library] the [instrumented library] or application to be
monitored. More information is available in the specification chapter [Obtaining a Tracer].

```java
Tracer tracer =
    openTelemetry.getTracer("instrumentation-library-name", "1.0.0");
```

Important: the "name" and optional version of the tracer are purely informational. 
All `Tracer`s that are created by a single `OpenTelemetry` instance will interoperate, regardless of name.

### Create a basic Span
To create a basic span, you only need to specify the name of the span.
The start and end time of the span is automatically set by the OpenTelemetry SDK.
```java
Span span = tracer.spanBuilder("my span").startSpan();
// put the span into the current Context
try (Scope scope = span.makeCurrent()) {
	// your use case
	...
} catch (Throwable t) {
    span.setStatus(StatusCode.ERROR, "Change it to your error message");
} finally {
    span.end(); // closing the scope does not end the span, this has to be done manually
}
```

### Create nested Spans

Most of the time, we want to correlate spans for nested operations. OpenTelemetry supports tracing
within processes and across remote processes. For more details how to share context between remote
processes, see [Context Propagation](#context-propagation).

For a method `a` calling a method `b`, the spans could be manually linked in the following way:
```java
void parentOne() {
  Span parentSpan = tracer.spanBuilder("parent").startSpan();
  try {
    childOne(parentSpan);
  } finally {
    parentSpan.end();
  }
}

void childOne(Span parentSpan) {
  Span childSpan = tracer.spanBuilder("child")
        .setParent(Context.current().with(parentSpan))
        .startSpan();
  // do stuff
  childSpan.end();
}
```
The OpenTelemetry API offers also an automated way to propagate the parent span on the current thread:
```java
void parentTwo() {
  Span parentSpan = tracer.spanBuilder("parent").startSpan();
  try(Scope scope = parentSpan.makeCurrent()) {
    childTwo();
  } finally {
    parentSpan.end();
  }
}
void childTwo() {
  Span childSpan = tracer.spanBuilder("child")
    // NOTE: setParent(...) is not required; 
    // `Span.current()` is automatically added as the parent
    .startSpan();
  try(Scope scope = childSpan.makeCurrent()) {
    // do stuff
  } finally {
    childSpan.end();
  }
}
``` 

To link spans from remote processes, it is sufficient to set the
[Remote Context](#context-propagation) as parent.

```java
Span childRemoteParent = tracer.spanBuilder("Child").setParent(remoteContext).startSpan();
```

### Span Attributes
In OpenTelemetry spans can be created freely and it's up to the implementor to annotate them with
attributes specific to the represented operation. Attributes provide additional context on a span
about the specific operation it tracks, such as results or operation properties.

```java
Span span = tracer.spanBuilder("/resource/path").setSpanKind(SpanKind.CLIENT).startSpan();
span.setAttribute("http.method", "GET");
span.setAttribute("http.url", url.toString());
```

Some of these operations represent calls that use well-known protocols like HTTP or database calls. 
For these, OpenTelemetry requires specific attributes to be set. The full attribute list is
available in the [Semantic Conventions](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/README.md) in the cross-language specification.

### Create Spans with events

Spans can be annotated with named events that can carry zero or more
[Span Attributes](#span-attributes), each of which is itself a key:value map paired automatically
with a timestamp.

```java
span.addEvent("Init");
...
span.addEvent("End");
```

```java
Attributes eventAttributes = Attributes.of(
    AttributeKey.stringKey("key"), "value",
    AttributeKey.longKey("result"), 0L);

span.addEvent("End Computation", eventAttributes);
```

### Create Spans with links
A Span may be linked to zero or more other Spans that are causally related. Links can be used to
represent batched operations where a Span was initiated by multiple initiating Spans, each
representing a single incoming item being processed in the batch.

```java
Span child = tracer.spanBuilder("childWithLink")
        .addLink(parentSpan1.getSpanContext())
        .addLink(parentSpan2.getSpanContext())
        .addLink(parentSpan3.getSpanContext())
        .addLink(remoteSpanContext)
    .startSpan();
```

For more details how to read context from remote processes, see
[Context Propagation](#context-propagation).

### Context Propagation

OpenTelemetry provides a text-based approach to propagate context to remote services using the
[W3C Trace Context](https://www.w3.org/TR/trace-context/) HTTP headers.

The following presents an example of an outgoing HTTP request using `HttpURLConnection`.
 
```java
// Tell OpenTelemetry to inject the context in the HTTP headers
TextMapSetter<HttpURLConnection> setter =
  new TextMapSetter<HttpURLConnection>() {
    @Override
    public void set(HttpURLConnection carrier, String key, String value) {
        // Insert the context as Header
        carrier.setRequestProperty(key, value);
    }
};

URL url = new URL("http://127.0.0.1:8080/resource");
Span outGoing = tracer.spanBuilder("/resource").setSpanKind(SpanKind.CLIENT).startSpan();
try (Scope scope = outGoing.makeCurrent()) {
  // Semantic Convention.
  // (Note that to set these, Span does not *need* to be the current instance in Context or Scope.)
  outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
  outGoing.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
  HttpURLConnection transportLayer = (HttpURLConnection) url.openConnection();
  // Inject the request with the *current*  Context, which contains our current Span.
  openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), transportLayer, setter);
  // Make outgoing call
} finally {
  outGoing.end();
}
...
```

Similarly, the text-based approach can be used to read the W3C Trace Context from incoming requests.
The following presents an example of processing an incoming HTTP request using
[HttpExchange](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpExchange.html).

```java
TextMapGetter<HttpExchange> getter =
  new TextMapGetter<>() {
    @Override
    public String get(HttpExchange carrier, String key) {
      if (carrier.getRequestHeaders().containsKey(key)) {
        return carrier.getRequestHeaders().get(key).get(0);
      }
      return null;
    }

   @Override
   public Iterable<String> keys(HttpExchange carrier) {
     return carrier.getRequestHeaders().keySet();
   } 
};
...
public void handle(HttpExchange httpExchange) {
  // Extract the SpanContext and other elements from the request.
  Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
        .extract(Context.current(), httpExchange, getter);
  try (Scope scope = extractedContext.makeCurrent()) {
    // Automatically use the extracted SpanContext as parent.
    Span serverSpan = tracer.spanBuilder("GET /resource")
        .setSpanKind(SpanKind.SERVER)
        .startSpan();
    try {
      // Add the attributes defined in the Semantic Conventions
      serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
      serverSpan.setAttribute(SemanticAttributes.HTTP_SCHEME, "http");
      serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, "localhost:8080");
      serverSpan.setAttribute(SemanticAttributes.HTTP_TARGET, "/resource");
      // Serve the request
      ...
    } finally {
      serverSpan.end();
    }
  }
}
```

## Metrics (alpha only!)

Spans are a great way to get detailed information about what your application is doing, but
what about a more aggregated perspective? OpenTelemetry provides supports for metrics, a time series
of numbers that might express things such as CPU utilization, request count for an HTTP server, or a
business metric such as transactions.

In order to access the alpha metrics library, you will need to explicitly depend on the `opentelemetry-api-metrics`
and `opentelemetry-sdk-metrics` modules, which are not included in the opentelemetry-bom until they are 
stable and ready for long-term-support.

All metrics can be annotated with labels: additional qualifiers that help describe what
subdivision of the measurements the metric represents.

First, you'll need to get access to a `MeterProvider`. Note the APIs for this are in flux, so no
example code is provided here for that.

The following is an example of counter usage:

```java
// Gets or creates a named meter instance
Meter meter = meterProvider.get("instrumentation-library-name", "1.0.0");

// Build counter e.g. LongCounter 
LongCounter counter = meter
        .longCounterBuilder("processed_jobs")
        .setDescription("Processed jobs")
        .setUnit("1")
        .build();

// It is recommended that the API user keep a reference to a Bound Counter for the entire time or 
// call unbind when no-longer needed.
BoundLongCounter someWorkCounter = counter.bind(Labels.of("Key", "SomeWork"));

// Record data
someWorkCounter.add(123);

// Alternatively, the user can use the unbounded counter and explicitly
// specify the labels set at call-time:
counter.add(123, Labels.of("Key", "SomeWork"));
```

`Observer` is an additional instrument supporting an asynchronous API and
collecting metric data on demand, once per collection interval.

The following is an example of observer usage:

```java
// Build observer e.g. LongSumObserver
    LongSumObserver observer = meter
        .longSumObserverBuilder("cpu_usage")
        .setDescription("CPU Usage")
        .setUnit("ms")
        .setUpdater(result -> {
        result.observe(getCpuUsage(), Labels.of("Key", "SomeWork"));
        })
        .build();
```

## Tracing SDK Configuration

The configuration examples reported in this document only apply to the SDK provided by
`opentelemetry-sdk`. Other implementation of the API might provide different configuration
mechanisms.

The application has to install a span processor with an exporter and may customize the behavior of
the OpenTelemetry SDK.

For example, a basic configuration instantiates the SDK tracer provider and sets to export the
traces to a logging stream.

```java
    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(BatchSpanProcessor.builder(new LoggingSpanExporter()).build())
      .build();
```

### Sampler

It is not always feasible to trace and export every user request in an application.
In order to strike a balance between observability and expenses, traces can be sampled. 

The OpenTelemetry SDK offers four samplers out of the box:
 - [AlwaysOnSampler] which samples every trace regardless of upstream sampling decisions.
 - [AlwaysOffSampler] which doesn't sample any trace, regardless of upstream sampling decisions.
 - [ParentBased] which uses the parent span to make sampling decisions, if present.
 - [TraceIdRatioBased] which samples a configurable percentage of traces, and additionally samples any
   trace that was sampled upstream.

Additional samplers can be provided by implementing the `io.opentelemetry.sdk.trace.Sampler`
interface.

```java
    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .setSampler(Sampler.alwaysOn())
      //or 
      .setSampler(Sampler.alwaysOff())
      //or
      .setSampler(Sampler.traceIdRatioBased(0.5))
      .build();
```

### Span Processor

Different Span processors are offered by OpenTelemetry. The `SimpleSpanProcessor` immediately
forwards ended spans to the exporter, while the `BatchSpanProcessor` batches them and sends them
in bulk. Multiple Span processors can be configured to be active at the same time using the
`MultiSpanProcessor`.

```java
    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
      .addSpanProcessor(BatchSpanProcessor.builder(new LoggingSpanExporter()).build())
      .build();
```

### Exporter

Span processors are initialized with an exporter which is responsible for sending the telemetry data
a particular backend. OpenTelemetry offers five exporters out of the box:
- In-Memory Exporter: keeps the data in memory, useful for debugging.
- Jaeger Exporter: prepares and sends the collected telemetry data to a Jaeger backend via gRPC.
- Zipkin Exporter: prepares and sends the collected telemetry data to a Zipkin backend via the Zipkin APIs.
- Logging Exporter: saves the telemetry data into log streams.
- OpenTelemetry Exporter: sends the data to the [OpenTelemetry Collector].

Other exporters can be found in the [OpenTelemetry Registry].

```java
    ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress("localhost", 3336)
      .usePlaintext()
      .build();

    JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
      .setEndpoint("localhost:3336")
      .setTimeout(30, TimeUnit.SECONDS)
      .build();

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
      .build();
```

### Auto Configuration

To configure the OpenTelemetry SDK based on the standard set of environment variables and system 
properties, you can use the `opentelemetry-sdk-extension-autoconfigure` module.

```java
  OpenTelemetrySdk sdk = OpenTelemetrySdkAutoConfiguration.initialize();
```

See the supported configuration options in the module's [README](./sdk-extensions/autoconfigure/README.md).

[AlwaysOnSampler]: https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/tracing/src/main/java/io/opentelemetry/sdk/trace/samplers/Sampler.java#L29
[AlwaysOffSampler]:https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/tracing/src/main/java/io/opentelemetry/sdk/trace/samplers/Sampler.java#L40
[ParentBased]:https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/tracing/src/main/java/io/opentelemetry/sdk/trace/samplers/Sampler.java#L54
[TraceIdRatioBased]:https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/tracing/src/main/java/io/opentelemetry/sdk/trace/samplers/Sampler.java#L78
[Library Guidelines]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/library-guidelines.md
[OpenTelemetry Collector]: https://github.com/open-telemetry/opentelemetry-collector
[OpenTelemetry Registry]: https://opentelemetry.io/registry/?s=exporter
[OpenTelemetry Website]: https://opentelemetry.io/
[Obtaining a Tracer]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/api.md#get-a-tracer
[Semantic Conventions]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/semantic_conventions
[Instrumentation Library]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/glossary.md#instrumentation-library
[instrumented library]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/glossary.md#instrumented-library

## Logging and Error Handling 

OpenTelemetry uses [java.util.logging](https://docs.oracle.com/javase/7/docs/api/java/util/logging/package-summary.html)
to log information about OpenTelemetry, including errors and warnings about misconfigurations or failures exporting 
data. 

By default, log messages are handled by the root handler in your application. If you have not 
installed a custom root handler for your application, logs of level `INFO` or higher are sent to the console by default. 

You may 
want to change the behavior of the logger for OpenTelemetry. For example, you can reduce the logging level 
to output additional information when debugging, increase the level for a particular class to ignore errors coming 
from that class, or install a custom handler or filter to run custom code whenever OpenTelemetry logs
a particular message. 

### Examples

```properties
## Turn off all OpenTelemetry logging 
io.opentelemetry.level = OFF
```

```properties
## Turn off logging for just the BatchSpanProcessor 
io.opentelemetry.sdk.trace.export.BatchSpanProcessor.level = OFF
```

```properties
## Log "FINE" messages for help in debugging 
io.opentelemetry.level = FINE

## Sets the default ConsoleHandler's logger's level 
## Note this impacts the logging outside of OpenTelemetry as well 
java.util.logging.ConsoleHandler.level = FINE

```

For more fine-grained control and special case handling, custom handlers and filters can be specified
with code. 

```java
// Custom filter which does not log errors that come from the export
public class IgnoreExportErrorsFilter implements Filter {

 public boolean isLoggable(LogRecord record) {
    return !record.getMessage().contains("Exception thrown by the export");
 }
}
```

```properties
## Registering the custom filter on the BatchSpanProcessor
io.opentelemetry.sdk.trace.export.BatchSpanProcessor = io.opentelemetry.extension.logging.IgnoreExportErrorsFilter
```
