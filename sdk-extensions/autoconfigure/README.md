# OpenTelemetry SDK Autoconfigure

This artifact implements environment-based autoconfiguration of the OpenTelemetry SDK. This can be
an alternative to programmatic configuration using the normal SDK builders.

All options support being passed as Java system properties, e.g., `-Dotel.traces.exporter=zipkin` or
environment variables, e.g., `OTEL_TRACES_EXPORTER=zipkin`.

## Contents

* [General notes](#general-notes)
* [Exporters](#exporters)
  + [OTLP exporter (both span and metric exporters)](#otlp-exporter-both-span-and-metric-exporters)
  + [Jaeger exporter](#jaeger-exporter)
  + [Zipkin exporter](#zipkin-exporter)
  + [Prometheus exporter](#prometheus-exporter)
  + [Logging exporter](#logging-exporter)
* [Trace context propagation](#propagator)
* [OpenTelemetry Resource](#opentelemetry-resource)
* [Batch span processor](#batch-span-processor)
* [Sampler](#sampler)
* [Span limits](#span-limits)
* [Interval metric reader](#interval-metric-reader)
* [Customizing the OpenTelemetry SDK](#customizing-the-opentelemetry-sdk)

## General notes

- The autoconfigure module registers Java shutdown hooks to shut down the SDK when appropriate. Please note that since this project uses
java.util.logging for all of it's logging, some of that logging may be suppressed during shutdown hooks. This is a bug in the JDK itself,
and not something we can control. If you require logging during shutdown hooks, please consider using `System.out` rather than a logging framework
that might shut itself down in a shutdown hook, thus suppressing your log messages. See this [JDK bug](https://bugs.openjdk.java.net/browse/JDK-8161253)
for more details.

## Exporters

The following configuration properties are common to all exporters:

| System property | Environment variable | Purpose                                                                                                                                                 |
|-----------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter   | OTEL_TRACES_EXPORTER        | The exporter to be used for tracing. Default is `otlp`. `none` means no autoconfigured exporter. |
| otel.metrics.exporter   | OTEL_METRICS_EXPORTER        | The exporter to be used for metrics. Default is `otlp`. `none` means no autoconfigured exporter. |

### OTLP exporter (both span and metric exporters)

The [OpenTelemetry Protocol (OTLP)](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/otlp.md) span and metric exporters

| System property              | Environment variable        | Description                                                               |
|------------------------------|-----------------------------|---------------------------------------------------------------------------|
| otel.traces.exporter=otlp (default) | OTEL_TRACES_EXPORTER=otlp          | Select the OpenTelemetry exporter for tracing (default)                                   |
| otel.metrics.exporter=otlp (default) | OTEL_METRICS_EXPORTER=otlp          | Select the OpenTelemetry exporter for metrics (default)                                   |
| otel.exporter.otlp.endpoint  | OTEL_EXPORTER_OTLP_ENDPOINT | The OTLP traces and metrics endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317`.            |
| otel.exporter.otlp.traces.endpoint  | OTEL_EXPORTER_OTLP_TRACES_ENDPOINT | The OTLP traces endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317`.            |
| otel.exporter.otlp.metrics.endpoint  | OTEL_EXPORTER_OTLP_METRICS_ENDPOINT | The OTLP metrics endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317`.            |
| otel.exporter.otlp.headers   | OTEL_EXPORTER_OTLP_HEADERS  | Key-value pairs separated by commas to pass as request headers.        |
| otel.exporter.otlp.timeout   | OTEL_EXPORTER_OTLP_TIMEOUT  | The maximum waiting time allowed to send each batch. Default is `10000`.  |

To configure the service name for the OTLP exporter, add the `service.name` key
to the OpenTelemetry Resource ([see below](#opentelemetry-resource)), e.g. `OTEL_RESOURCE_ATTRIBUTES=service.name=myservice`.

### Jaeger exporter

The [Jaeger](https://www.jaegertracing.io/docs/1.21/apis/#protobuf-via-grpc-stable) exporter. This exporter uses gRPC for its communications protocol.

| System property                   | Environment variable              | Description                                                                                        |
|-----------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------------|
| otel.traces.exporter=jaeger       | OTEL_TRACES_EXPORTER=jaeger       | Select the Jaeger exporter                                                                         |
| otel.exporter.jaeger.endpoint     | OTEL_EXPORTER_JAEGER_ENDPOINT     | The Jaeger gRPC endpoint to connect to. Default is `http://localhost:14250`.                       |
| otel.exporter.jaeger.timeout      | OTEL_EXPORTER_JAEGER_TIMEOUT      | The maximum waiting time, in milliseconds, allowed to send each batch. Default is `10000`.         |

### Zipkin exporter

The [Zipkin](https://zipkin.io/zipkin-api/) exporter. It sends JSON in [Zipkin format](https://zipkin.io/zipkin-api/#/default/post_spans) to a specified HTTP URL.

| System property                   | Environment variable              | Description                                                                                                               |
|-----------------------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter=zipkin              | OTEL_TRACES_EXPORTER=zipkin              | Select the Zipkin exporter                                                                                             |
| otel.exporter.zipkin.endpoint     | OTEL_EXPORTER_ZIPKIN_ENDPOINT     | The Zipkin endpoint to connect to. Default is `http://localhost:9411/api/v2/spans`. Currently only HTTP is supported. |

### Prometheus exporter

The [Prometheus](https://github.com/prometheus/docs/blob/master/content/docs/instrumenting/exposition_formats.md) exporter.

| System property               | Environment variable          | Description                                                                        |
|-------------------------------|-------------------------------|------------------------------------------------------------------------------------|
| otel.metrics.exporter=prometheus      | OTEL_METRICS_EXPORTER=prometheus      | Select the Prometheus exporter                                                     |
| otel.exporter.prometheus.port | OTEL_EXPORTER_PROMETHEUS_PORT | The local port used to bind the prometheus metric server. Default is `9464`.       |
| otel.exporter.prometheus.host | OTEL_EXPORTER_PROMETHEUS_HOST | The local address used to bind the prometheus metric server. Default is `0.0.0.0`. |

Note that this is a pull exporter - it opens up a server on the local process listening on the specified host and port, which
a Prometheus server scrapes from.

### Logging exporter

The logging exporter prints the name of the span along with its attributes to stdout. It's mainly used for testing and debugging.

| System property              | Environment variable         | Description                                                                  |
|------------------------------|------------------------------|------------------------------------------------------------------------------|
| otel.traces.exporter=logging        | OTEL_TRACES_EXPORTER=logging        | Select the logging exporter for tracing                                               |
| otel.metrics.exporter=logging        | OTEL_METRICS_EXPORTER=logging        | Select the logging exporter for metrics                                               |
| otel.exporter.logging.prefix | OTEL_EXPORTER_LOGGING_PREFIX | An optional string printed in front of the span name and attributes.         |

## Propagator

The propagators determine which distributed tracing header formats are used, and which baggage propagation header formats are used.

| System property  | Environment variable | Description                                                                                                     |
|------------------|----------------------|-----------------------------------------------------------------------------------------------------------------|
| otel.propagators | OTEL_PROPAGATORS     | The propagators to be used. Use a comma-separated list for multiple propagators. Default is `tracecontext,baggage` (W3C). |

Supported values are

- `"tracecontext"`: [W3C Trace Context](https://www.w3.org/TR/trace-context/) (add `baggage` as well to include W3C baggage)
- `"baggage"`: [W3C Baggage](https://www.w3.org/TR/baggage/)
- `"b3"`: [B3 Single](https://github.com/openzipkin/b3-propagation#single-header)
- `"b3multi"`: [B3 Multi](https://github.com/openzipkin/b3-propagation#multiple-headers)
- `"jaeger"`: [Jaeger](https://www.jaegertracing.io/docs/1.21/client-libraries/#propagation-format) (includes Jaeger baggage)
- `"xray"`: [AWS X-Ray](https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader)
- `"ottrace"`: [OT Trace](https://github.com/opentracing?q=basic&type=&language=)

## OpenTelemetry Resource

The [OpenTelemetry Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/resource/sdk.md)
is a representation of the entity producing telemetry.

| System property          | Environment variable     | Description                                                                        |
|--------------------------|--------------------------|------------------------------------------------------------------------------------|
| otel.resource.attributes | OTEL_RESOURCE_ATTRIBUTES | Specify resource attributes in the following format: key1=val1,key2=val2,key3=val3 |

You almost always want to specify the [`service.name`](https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/resource/semantic_conventions#service) for your application.
It corresponds to how you describe the application, for example `authservice` could be an application that authenticates requests, and `cats` could be an application that returns information about [cats](https://en.wikipedia.org/wiki/Cat).
This means you would specify resource attributes something like `OTEL_RESOURCE_ATTRIBUTES=service.name=authservice`, or `-Dotel.resource.attributes=service.name=cats,service.namespace=mammals`.

## Batch span processor

| System property           | Environment variable      | Description                                                                        |
|---------------------------|---------------------------|------------------------------------------------------------------------------------|
| otel.bsp.schedule.delay   | OTEL_BSP_SCHEDULE_DELAY   | The interval, in milliseconds, between two consecutive exports. Default is `5000`. |
| otel.bsp.max.queue.size   | OTEL_BSP_MAX_QUEUE_SIZE   | The maximum queue size. Default is `2048`.                                             |
| otel.bsp.max.export.batch.size | OTEL_BSP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                                              |
| otel.bsp.export.timeout   | OTEL_BSP_EXPORT_TIMEOUT   | The maximum allowed time, in milliseconds, to export data. Default is `30000`.         |

## Sampler

The sampler configures whether spans will be recorded for any call to `SpanBuilder.startSpan`.

| System property                 | Environment variable            | Description                                                  |
|---------------------------------|---------------------------------|--------------------------------------------------------------|
| otel.traces.sampler              | OTEL_TRACES_SAMPLER              | The sampler to use for tracing. Defaults to `parentbased_always_on` |
| otel.traces.sampler.arg          | OTEL_TRACES_SAMPLER_ARG          | An argument to the configured tracer if supported, for example a ratio. |

Supported values for `otel.traces.sampler` are

- "always_on": AlwaysOnSampler
- "always_off": AlwaysOffSampler
- "traceidratio": TraceIdRatioBased. `otel.traces.sampler.arg` sets the ratio.
- "parentbased_always_on": ParentBased(root=AlwaysOnSampler)
- "parentbased_always_off": ParentBased(root=AlwaysOffSampler)
- "parentbased_traceidratio": ParentBased(root=TraceIdRatioBased). `otel.traces.sampler.arg` sets the ratio.

## Span limits

These properties can be used to control the maximum size of recordings per span.

| System property                 | Environment variable            | Description                                                  |
|---------------------------------|---------------------------------|--------------------------------------------------------------|
| otel.span.attribute.count.limit | OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT | The maximum number of attributes per span. Default is `128`.  |
| otel.span.event.count.limit     | OTEL_SPAN_EVENT_COUNT_LIMIT     | The maximum number of events per span. Default is `128`.     |
| otel.span.link.count.limit      | OTEL_SPAN_LINK_COUNT_LIMIT      | The maximum number of links per span. Default is `128`        |

## Interval metric reader

| System property          | Environment variable     | Description                                                                       |
|--------------------------|--------------------------|-----------------------------------------------------------------------------------|
| otel.imr.export.interval | OTEL_IMR_EXPORT_INTERVAL | The interval, in milliseconds, between pushes to the exporter. Default is `60000`.|

## Customizing the OpenTelemetry SDK

Autoconfiguration exposes SPI [hooks](./src/main/java/io/opentelemetry/sdk/autoconfigure/spi) for customizing behavior programmatically as needed.
It's recommended to use the above configuration properties where possible, only implementing the SPI to add functionality not found in the
SDK by default.
