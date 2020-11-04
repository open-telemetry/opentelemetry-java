# OpenTelemetry QuickStart

<!-- Re-generate TOC with `markdown-toc --no-first-h1 -i` -->

<!-- toc -->

- [Tracing](#tracing)
  * [Create basic Span](#create-basic-span)
  * [Create nested Spans](#create-nested-spans)
  * [Span Attributes](#span-attributes)
  * [Create Spans with events](#create-spans-with-events)
  * [Create Spans with links](#create-spans-with-links)
  * [Context Propagation](#context-propagation)
- [Metrics](#metrics)
- [Tracing SDK Configuration](#tracing-sdk-configuration)
  * [Sampler](#sampler)
  * [Span Processor](#span-processor)
  * [Exporter](#exporter)
  * [TraceConfig](#traceconfig)
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

## Tracing

In the following, we present how to trace code using the OpenTelemetry API. **Note:** Methods of the
OpenTelemetry SDK should never be called.
 
First, a `Tracer` must be acquired, which is responsible for creating spans and interacting with the
[Context](#context-propagation). A tracer is acquired by using the OpenTelemetry API specifying the
name and version of the [library instrumenting][Instrumentation Library] the [instrumented library] or application to be
monitored. More information is available in the specification chapter [Obtaining a Tracer].

```java
Tracer tracer =
    OpenTelemetry.getTracer("instrumentation-library-name","semver:1.0.0");
```

### Create basic Span
To create a basic span, you only need to specify the name of the span.
The start and end time of the span is automatically set by the OpenTelemetry SDK.
```java
Span span = tracer.spanBuilder("my span").startSpan();
try (Scope scope = TracingContextUtils.currentContextWith(span)) {
	// your use case
	...
} catch (Throwable t) {
    span.setStatus(StatusCanonicalCode.ERROR, "Change it to your error message");
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
void a() {
  Span parentSpan = tracer.spanBuilder("a")
        .startSpan();
  b(parentSpan);
  parentSpan.end();
}
void b(Span parentSpan) {
  Span childSpan = tracer.spanBuilder("b")
        .setParent(parentSpan)
        .startSpan();
  // do stuff
  childSpan.end();
}
```
The OpenTelemetry API offers also an automated way to propagate the `parentSpan`:
```java
void a() {
  Span parentSpan = tracer.spanBuilder("a").startSpan();
  try(Scope scope = TracingContextUtils.currentContextWith(parentSpan)) {
    b();
  } finally {
    parentSpan.end();
  }
}
void b() {
  Span childSpan = tracer.spanBuilder("b")
    // NOTE: setParent(parentSpan) is not required; 
    // `TracingContextUtils.getCurrentSpan()` is automatically added as parent
    .startSpan();
  try(Scope scope = TracingContextUtils.currentContextWith(childSpan)) {
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
Span span = tracer.spanBuilder("/resource/path").setSpanKind(Span.Kind.CLIENT).startSpan();
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
        .addLink(parentSpan1.getContext())
        .addLink(parentSpan2.getContext())
        .addLink(parentSpan3.getContext())
        .addLink(remoteContext)
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
TextMapPropagator.Setter<HttpURLConnection> setter =
  new TextMapPropagator.Setter<HttpURLConnection>() {
    @Override
    public void put(HttpURLConnection carrier, String key, String value) {
        // Insert the context as Header
        carrier.setRequestProperty(key, value);
    }
};

URL url = new URL("http://127.0.0.1:8080/resource");
Span outGoing = tracer.spanBuilder("/resource").setSpanKind(Span.Kind.CLIENT).startSpan();
try (Scope scope = TracingContextUtils.currentContextWith(outGoing)) {
  // Semantic Convention.
  // (Observe that to set these, Span does not *need* to be the current instance.)
  outGoing.setAttribute("http.method", "GET");
  outGoing.setAttribute("http.url", url.toString());
  HttpURLConnection transportLayer = (HttpURLConnection) url.openConnection();
  // Inject the request with the *current*  Context, which contains our current Span.
  OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), transportLayer, setter);
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
TextMapPropagator.Getter<HttpExchange> getter =
  new TextMapPropagator.Getter<HttpExchange>() {
    @Override
    public String get(HttpExchange carrier, String key) {
      if (carrier.getRequestHeaders().containsKey(key)) {
        return carrier.getRequestHeaders().get(key).get(0);
      }
      return null;
    }
};
...
public void handle(HttpExchange httpExchange) {
  // Extract the SpanContext and other elements from the request.
  Context extractedContext = OpenTelemetry.getPropagators().getTextMapPropagator()
        .extract(Context.current(), httpExchange, getter);
  Span serverSpan = null;
  try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
    // Automatically use the extracted SpanContext as parent.
    serverSpan = tracer.spanBuilder("/resource").setSpanKind(Span.Kind.SERVER)
        .startSpan();
    // Add the attributes defined in the Semantic Conventions
    serverSpan.setAttribute("http.method", "GET");
    serverSpan.setAttribute("http.scheme", "http");
    serverSpan.setAttribute("http.host", "localhost:8080");
    serverSpan.setAttribute("http.target", "/resource");
    // Serve the request
    ...
  } finally {
    if (serverSpan != null) {
      serverSpan.end();
    }
  }
}
```

## Metrics

Spans are a great way to get detailed information about what your application is doing, but
what about a more aggregated perspective? OpenTelemetry provides supports for metrics, a time series
of numbers that might express things such as CPU utilization, request count for an HTTP server, or a
business metric such as transactions.

All metrics can be annotated with labels: additional qualifiers that help describe what
subdivision of the measurements the metric represents.

The following is an example of counter usage:

```java
// Gets or creates a named meter instance
Meter meter = OpenTelemetry.getMeter("instrumentation-library-name","semver:1.0.0");

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
// Build observer e.g. LongObserver
LongObserver observer = meter
        .observerLongBuilder("cpu_usage")
        .setDescription("CPU Usage")
        .setUnit("ms")
        .build();

observer.setCallback(
        new LongObserver.Callback<LongObserver.ResultLongObserver>() {
          @Override
          public void update(ResultLongObserver result) {
            // long getCpuUsage()
            result.observe(getCpuUsage(), Labels.of("Key", "SomeWork"));
          }
        });
```

## Tracing SDK Configuration

The configuration examples reported in this document only apply to the SDK provided by
`opentelemetry-sdk`. Other implementation of the API might provide different configuration
mechanisms.

The application has to install a span processor with an exporter and may customize the behavior of
the OpenTelemetry SDK.

For example, a basic configuration instantiates the SDK tracer registry and sets to export the
traces to a logging stream.

```java
// Get the tracer
TracerSdkManagement tracerSdkManagement = OpenTelemetrySdk.getTracerManagement();

// Set to export the traces to a logging stream
tracerSdkManagement.addSpanProcessor(
    SimpleSpanProcessor.builder(
        new LoggingSpanExporter()
    ).build());
```

### Sampler

It is not always feasible to trace and export every user request in an application.
In order to strike a balance between observability and expenses, traces can be sampled. 

The OpenTelemetry SDK offers three samplers out of the box:
 - [AlwaysOnSampler] which samples every trace regardless of upstream sampling decisions.
 - [AlwaysOffSampler] which doesn't sample any trace, regardless of upstream sampling decisions.
 - [Probability] which samples a configurable percentage of traces, and additionally samples any
   trace that was sampled upstream.

Additional samplers can be provided by implementing the `io.opentelemetry.sdk.trace.Sampler`
interface.

```java
TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.alwaysOn()
).build();
TraceConfig alwaysOff = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.alwaysOff()
).build();
TraceConfig half = TraceConfig.getDefault().toBuilder().setSampler(
        Samplers.probability(0.5)
).build();
// Configure the sampler to use
tracerProvider.updateActiveTraceConfig(
    half
);
```

### Span Processor

Different Span processors are offered by OpenTelemetry. The `SimpleSpanProcessor` immediately
forwards ended spans to the exporter, while the `BatchSpanProcessor` batches them and sends them
in bulk. Multiple Span processors can be configured to be active at the same time using the
`MultiSpanProcessor`.

```java
tracerSdkManagement.addSpanProcessor(
    SimpleSpanProcessor.builder(new LoggingSpanExporter()).build()
);
tracerSdkManagement.addSpanProcessor(
    BatchSpanProcessor.builder(new LoggingSpanExporter()).build()
);
tracerSdkManagement.addSpanProcessor(MultiSpanProcessor.create(Arrays.asList(
            SimpleSpanProcessor.builder(new LoggingSpanExporter()).build(),
            BatchSpanProcessor.builder(new LoggingSpanExporter()).build()
)));
```

### Exporter

Span processors are initialized with an exporter which is responsible for sending the telemetry data
a particular backend. OpenTelemetry offers four exporters out of the box:
- In-Memory Exporter: keeps the data in memory, useful for debugging.
- Jaeger Exporter: prepares and sends the collected telemetry data to a Jaeger backend via gRPC.
- Zipkin Exporter: prepares and sends the collected telemetry data to a Zipkin backend via the Zipkin APIs.
- Logging Exporter: saves the telemetry data into log streams.
- OpenTelemetry Exporter: sends the data to the [OpenTelemetry Collector] (not yet implemented).

Other exporters can be found in the [OpenTelemetry Registry].

```java
tracerSdkManagement.addSpanProcessor(
    SimpleSpanProcessor.builder(InMemorySpanExporter.create()).build());
tracerSdkManagement.addSpanProcessor(
    SimpleSpanProcessor.builder(new LoggingSpanExporter()).build());

ManagedChannel jaegerChannel =
    ManagedChannelBuilder.forAddress([ip:String], [port:int]).usePlaintext().build();
JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
    .setServiceName("example").setChannel(jaegerChannel).setDeadline(30000)
    .build();
tracerSdkManagement.addSpanProcessor(BatchSpanProcessor.builder(
    jaegerExporter
).build());
```

### TraceConfig

The `TraceConfig` associated with a Tracer SDK can be updated via system properties, 
environment variables and builder `set*` methods.  

```java
// Get TraceConfig associated with TracerSdk 
TracerConfig traceConfig = OpenTelemetrySdk.getTracerManagement().getActiveTraceConfig();

// Get TraceConfig Builder
Builder builder = traceConfig.toBuilder();

// Read configuration options from system properties
builder.readSystemProperties();

// Read configuration options from environment variables
builder.readEnvironmentVariables()

// Set options via builder.set* methods, e.g.
builder.setMaxNumberOfLinks(10);

// Update the resulting TraceConfig instance
OpenTelemetrySdk.getTracerManagement().updateActiveTraceConfig(builder.build());
```

Supported system properties and environment variables:

| System property                  | Environment variable             | Purpose                                                                                             | 
|----------------------------------|----------------------------------|-----------------------------------------------------------------------------------------------------|       
| otel.config.sampler.probability  | OTEL_CONFIG_SAMPLER_PROBABILITY  | Sampler which is used when constructing a new span (default: 1)                                     |                        
| otel.span.attribute.count.limit  | OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT  | Max number of attributes per span, extra will be dropped (default: 1000)                              |                        
| otel.span.event.count.limit      | OTEL_SPAN_EVENT_COUNT_LIMIT      | Max number of Events per span, extra will be dropped (default: 1000)                                 |                        
| otel.span.link.count.limit       | OTEL_SPAN_LINK_COUNT_LIMIT       | Max number of Link entries per span, extra will be dropped (default: 1000)                            |
| otel.config.max.event.attrs      | OTEL_CONFIG_MAX_EVENT_ATTRS      | Max number of attributes per event, extra will be dropped (default: 32)                             |
| otel.config.max.link.attrs       | OTEL_CONFIG_MAX_LINK_ATTRS       | Max number of attributes per link, extra will be dropped  (default: 32)                             |
| otel.config.max.attr.length      | OTEL_CONFIG_MAX_ATTR_LENGTH      | Max length of string attribute value in characters, too long will be truncated (default: unlimited) |

[AlwaysOnSampler]: https://github.com/open-telemetry/opentelemetry-java/blob/master/sdk/src/main/java/io/opentelemetry/sdk/trace/Samplers.java#L82--L105
[AlwaysOffSampler]:https://github.com/open-telemetry/opentelemetry-java/blob/master/sdk/src/main/java/io/opentelemetry/sdk/trace/Samplers.java#L108--L131
[Probability]:https://github.com/open-telemetry/opentelemetry-java/blob/master/sdk/src/main/java/io/opentelemetry/sdk/trace/Samplers.java#L142--L203
[Library Guidelines]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/library-guidelines.md
[OpenTelemetry Collector]: https://github.com/open-telemetry/opentelemetry-collector
[OpenTelemetry Registry]: https://opentelemetry.io/registry/?s=exporter
[OpenTelemetry Website]: https://opentelemetry.io/
[Obtaining a Tracer]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/api.md#get-a-tracer
[Semantic Conventions]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions
[Instrumentation Library]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/glossary.md#instrumentation-library
[instrumented library]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/glossary.md#instrumented-library

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
