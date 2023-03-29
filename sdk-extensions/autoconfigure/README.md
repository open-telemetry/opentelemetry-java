# OpenTelemetry SDK Autoconfigure

This artifact implements environment-based autoconfiguration of the OpenTelemetry SDK. This can be
an alternative to programmatic configuration using the normal SDK builders.

All options support being passed as Java system properties, e.g., `-Dotel.traces.exporter=zipkin` or
environment variables, e.g., `OTEL_TRACES_EXPORTER=zipkin`.

## Contents

* [General notes](#general-notes)
* [Disabling OpenTelemetrySdk](#disabling-opentelemetrysdk)
* [Exporters](#exporters)
  + [OTLP exporter (span, metric, and log exporters)](#otlp-exporter-span-metric-and-log-exporters)
    + [OTLP exporter retry](#otlp-exporter-retry)
  + [Jaeger exporter](#jaeger-exporter)
  + [Zipkin exporter](#zipkin-exporter)
  + [Prometheus exporter](#prometheus-exporter)
  + [Logging exporter](#logging-exporter)
* [Trace context propagation](#propagator)
* [OpenTelemetry Resource](#opentelemetry-resource)
  + [Resource Provider SPI](#resource-provider-spi)
  + [Disabling automatic ResourceProviders](#disabling-automatic-resourceproviders)
* [Batch span processor](#batch-span-processor)
* [Sampler](#sampler)
* [Attribute limits](#attribute-limits)
* [Span limits](#span-limits)
* [Exemplars](#exemplars)
* [Periodic Metric Reader](#periodic-metric-reader)
* [Batch log record processor](#batch-log-record-processor)
* [Customizing the OpenTelemetry SDK](#customizing-the-opentelemetry-sdk)

## General notes

- The autoconfigure module registers Java shutdown hooks to shut down the SDK when appropriate. Please note that since this project uses
java.util.logging for all of it's logging, some of that logging may be suppressed during shutdown hooks. This is a bug in the JDK itself,
and not something we can control. If you require logging during shutdown hooks, please consider using `System.out` rather than a logging framework
that might shut itself down in a shutdown hook, thus suppressing your log messages. See this [JDK bug](https://bugs.openjdk.java.net/browse/JDK-8161253)
for more details.

## Disabling OpenTelemetrySdk

The OpenTelemetry SDK can be disabled entirely. If disabled, `AutoConfiguredOpenTelemetrySdk#getOpenTelemetrySdk()` will return a minimally configured instance (i.e. `OpenTelemetrySdk.builder().build()`).

| System property   | Environment variable | Purpose                                                        |
|-------------------|----------------------|----------------------------------------------------------------|
| otel.sdk.disabled | OTEL_SDK_DISABLED    | If `true`, disable the OpenTelemetry SDK. Defaults to `false`. |

## Exporters

The following configuration properties are common to all exporters:

| System property       | Environment variable  | Purpose                                                                                                                    |
|-----------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter  | OTEL_TRACES_EXPORTER  | List of exporters to be used for tracing, separated by commas. Default is `otlp`. `none` means no autoconfigured exporter. |
| otel.metrics.exporter | OTEL_METRICS_EXPORTER | List of exporters to be used for metrics, separated by commas. Default is `otlp`. `none` means no autoconfigured exporter. |
| otel.logs.exporter    | OTEL_LOGS_EXPORTER    | List of exporters to be used for logging, separated by commas. Default is `none`.                                          |

### OTLP exporter (span, metric, and log exporters)

The [OpenTelemetry Protocol (OTLP)](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/otlp.md) span, metric, and log exporters

| System property                                          | Environment variable                                     | Description                                                                                                                                                                                                                                                                                                                                                                                                          |
|----------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter=otlp (default)                      | OTEL_TRACES_EXPORTER=otlp                                | Select the OpenTelemetry exporter for tracing (default)                                                                                                                                                                                                                                                                                                                                                              |
| otel.metrics.exporter=otlp (default)                     | OTEL_METRICS_EXPORTER=otlp                               | Select the OpenTelemetry exporter for metrics (default)                                                                                                                                                                                                                                                                                                                                                              |
| otel.logs.exporter=otlp                                  | OTEL_LOGS_EXPORTER=otlp                                  | Select the OpenTelemetry exporter for logs                                                                                                                                                                                                                                                                                                                                                                           |
| otel.exporter.otlp.endpoint                              | OTEL_EXPORTER_OTLP_ENDPOINT                              | The OTLP traces, metrics, and logs endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. If protocol is `http/protobuf` the version and signal will be appended to the path (e.g. `v1/traces`, `v1/metrics`, or `v1/logs`). Default is `http://localhost:4317` when protocol is `grpc`, and `http://localhost:4318/v1/{signal}` when protocol is `http/protobuf`. |
| otel.exporter.otlp.traces.endpoint                       | OTEL_EXPORTER_OTLP_TRACES_ENDPOINT                       | The OTLP traces endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317` when protocol is `grpc`, and `http://localhost:4318/v1/traces` when protocol is `http/protobuf`.                                                                                                                                                         |
| otel.exporter.otlp.metrics.endpoint                      | OTEL_EXPORTER_OTLP_METRICS_ENDPOINT                      | The OTLP metrics endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317` when protocol is `grpc`, and `http://localhost:4318/v1/metrics` when protocol is `http/protobuf`.                                                                                                                                                       |
| otel.exporter.otlp.logs.endpoint                         | OTEL_EXPORTER_OTLP_LOGS_ENDPOINT                         | The OTLP logs endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4317` when protocol is `grpc`, and `http://localhost:4318/v1/logs` when protocol is `http/protobuf`.                                                                                                                                                             |
| otel.exporter.otlp.certificate                           | OTEL_EXPORTER_OTLP_CERTIFICATE                           | The path to the file containing trusted certificates to use when verifying an OTLP trace, metric, or log server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default the host platform's trusted root certificates are used.                                                                                                                                          |
| otel.exporter.otlp.traces.certificate                    | OTEL_EXPORTER_OTLP_TRACES_CERTIFICATE                    | The path to the file containing trusted certificates to use when verifying an OTLP trace server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default the host platform's trusted root certificates are used.                                                                                                                                                          |
| otel.exporter.otlp.metrics.certificate                   | OTEL_EXPORTER_OTLP_METRICS_CERTIFICATE                   | The path to the file containing trusted certificates to use when verifying an OTLP metric server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default the host platform's trusted root certificates are used.                                                                                                                                                         |
| otel.exporter.otlp.logs.certificate                      | OTEL_EXPORTER_OTLP_LOGS_CERTIFICATE                      | The path to the file containing trusted certificates to use when verifying an OTLP log server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default the host platform's trusted root certificates are used.                                                                                                                                                            |
| otel.exporter.otlp.client.key                            | OTEL_EXPORTER_OTLP_CLIENT_KEY                            | The path to the file containing private client key to use when verifying an OTLP trace, metric, or log client's TLS credentials. The file should contain one private key PKCS8 PEM format. By default no client key is used.                                                                                                                                                                                         |
| otel.exporter.otlp.traces.client.key                     | OTEL_EXPORTER_OTLP_TRACES_CLIENT_KEY                     | The path to the file containing private client key to use when verifying an OTLP trace client's TLS credentials. The file should contain one private key PKCS8 PEM format. By default no client key file is used.                                                                                                                                                                                                    |
| otel.exporter.otlp.metrics.client.key                    | OTEL_EXPORTER_OTLP_METRICS_CLIENT_KEY                    | The path to the file containing private client key to use when verifying an OTLP metric client's TLS credentials. The file should contain one private key PKCS8 PEM format. By default no client key file is used.                                                                                                                                                                                                   |
| otel.exporter.otlp.logs.client.key                       | OTEL_EXPORTER_OTLP_LOGS_CLIENT_KEY                       | The path to the file containing private client key to use when verifying an OTLP log client's TLS credentials. The file should contain one private key PKCS8 PEM format. By default no client key file is used.                                                                                                                                                                                                      |
| otel.exporter.otlp.client.certificate                    | OTEL_EXPORTER_OTLP_CLIENT_CERTIFICATE                    | The path to the file containing trusted certificates to use when verifying an OTLP trace, metric, or log client's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default no chain file is used.                                                                                                                                                                           |
| otel.exporter.otlp.traces.client.certificate             | OTEL_EXPORTER_OTLP_TRACES_CLIENT_CERTIFICATE             | The path to the file containing trusted certificates to use when verifying an OTLP trace server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default no chain file is used.                                                                                                                                                                                           |
| otel.exporter.otlp.metrics.client.certificate            | OTEL_EXPORTER_OTLP_METRICS_CLIENT_CERTIFICATE            | The path to the file containing trusted certificates to use when verifying an OTLP metric server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default no chain file is used.                                                                                                                                                                                          |
| otel.exporter.otlp.logs.client.certificate               | OTEL_EXPORTER_OTLP_LOGS_CLIENT_CERTIFICATE               | The path to the file containing trusted certificates to use when verifying an OTLP log server's TLS credentials. The file should contain one or more X.509 certificates in PEM format. By default no chain file is used.                                                                                                                                                                                             |
| otel.exporter.otlp.headers                               | OTEL_EXPORTER_OTLP_HEADERS                               | Key-value pairs separated by commas to pass as request headers on OTLP trace, metric, and log requests.                                                                                                                                                                                                                                                                                                              |
| otel.exporter.otlp.traces.headers                        | OTEL_EXPORTER_OTLP_TRACES_HEADERS                        | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                                                                                                                                                                                                                                                                                                                               |
| otel.exporter.otlp.metrics.headers                       | OTEL_EXPORTER_OTLP_METRICS_HEADERS                       | Key-value pairs separated by commas to pass as request headers on OTLP metrics requests.                                                                                                                                                                                                                                                                                                                             |
| otel.exporter.otlp.logs.headers                          | OTEL_EXPORTER_OTLP_LOGS_HEADERS                          | Key-value pairs separated by commas to pass as request headers on OTLP logs requests.                                                                                                                                                                                                                                                                                                                                |
| otel.exporter.otlp.compression                           | OTEL_EXPORTER_OTLP_COMPRESSION                           | The compression type to use on OTLP trace, metric, and log requests. Options include `gzip`. By default no compression will be used.                                                                                                                                                                                                                                                                                 |
| otel.exporter.otlp.traces.compression                    | OTEL_EXPORTER_OTLP_TRACES_COMPRESSION                    | The compression type to use on OTLP trace requests. Options include `gzip`. By default no compression will be used.                                                                                                                                                                                                                                                                                                  |
| otel.exporter.otlp.metrics.compression                   | OTEL_EXPORTER_OTLP_METRICS_COMPRESSION                   | The compression type to use on OTLP metric requests. Options include `gzip`. By default no compression will be used.                                                                                                                                                                                                                                                                                                 |
| otel.exporter.otlp.logs.compression                      | OTEL_EXPORTER_OTLP_LOGS_COMPRESSION                      | The compression type to use on OTLP log requests. Options include `gzip`. By default no compression will be used.                                                                                                                                                                                                                                                                                                    |
| otel.exporter.otlp.timeout                               | OTEL_EXPORTER_OTLP_TIMEOUT                               | The maximum waiting time, in milliseconds, allowed to send each OTLP trace, metric, and log batch. Default is `10000`.                                                                                                                                                                                                                                                                                               |
| otel.exporter.otlp.traces.timeout                        | OTEL_EXPORTER_OTLP_TRACES_TIMEOUT                        | The maximum waiting time, in milliseconds, allowed to send each OTLP trace batch. Default is `10000`.                                                                                                                                                                                                                                                                                                                |
| otel.exporter.otlp.metrics.timeout                       | OTEL_EXPORTER_OTLP_METRICS_TIMEOUT                       | The maximum waiting time, in milliseconds, allowed to send each OTLP metric batch. Default is `10000`.                                                                                                                                                                                                                                                                                                               |
| otel.exporter.otlp.logs.timeout                          | OTEL_EXPORTER_OTLP_LOGS_TIMEOUT                          | The maximum waiting time, in milliseconds, allowed to send each OTLP log batch. Default is `10000`.                                                                                                                                                                                                                                                                                                                  |
| otel.exporter.otlp.protocol                              | OTEL_EXPORTER_OTLP_PROTOCOL                              | The transport protocol to use on OTLP trace, metric, and log requests. Options include `grpc` and `http/protobuf`. Default is `grpc`.                                                                                                                                                                                                                                                                                |
| otel.exporter.otlp.traces.protocol                       | OTEL_EXPORTER_OTLP_TRACES_PROTOCOL                       | The transport protocol to use on OTLP trace requests. Options include `grpc` and `http/protobuf`. Default is `grpc`.                                                                                                                                                                                                                                                                                                 |
| otel.exporter.otlp.metrics.protocol                      | OTEL_EXPORTER_OTLP_METRICS_PROTOCOL                      | The transport protocol to use on OTLP metric requests. Options include `grpc` and `http/protobuf`. Default is `grpc`.                                                                                                                                                                                                                                                                                                |
| otel.exporter.otlp.logs.protocol                         | OTEL_EXPORTER_OTLP_LOGS_PROTOCOL                         | The transport protocol to use on OTLP log requests. Options include `grpc` and `http/protobuf`. Default is `grpc`.                                                                                                                                                                                                                                                                                                   |
| otel.exporter.otlp.metrics.temporality.preference        | OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE        | The preferred output aggregation temporality. Options include `DELTA` and `CUMULATIVE`. If `CUMULATIVE`, all instruments will have cumulative temporality. If `DELTA`, counter (sync and async) and histograms will be delta, up down counters (sync and async) will be cumulative. Default is `CUMULATIVE`.                                                                                                         |
| otel.exporter.otlp.metrics.default.histogram.aggregation | OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION | The preferred default histogram aggregation. Options include `BASE2_EXPONENTIAL_BUCKET_HISTOGRAM` and `EXPLICIT_BUCKET_HISTOGRAM`. Default is `EXPLICIT_BUCKET_HISTOGRAM`.                                                                                                                                                                                                                                           |
| otel.experimental.exporter.otlp.retry.enabled            | OTEL_EXPERIMENTAL_EXPORTER_OTLP_RETRY_ENABLED            | If `true`, enable [experimental retry support](#otlp-exporter-retry). Default is `false`.                                                                                                                                                                                                                                                                                                                            |

To configure the service name for the OTLP exporter, add the `service.name` key
to the OpenTelemetry Resource ([see below](#opentelemetry-resource)), e.g. `OTEL_RESOURCE_ATTRIBUTES=service.name=myservice`.

#### OTLP exporter retry

[OTLP](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/otlp.md#otlpgrpc-response) requires that [transient](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#retry) errors be handled with a retry strategy. When retry is enabled, retryable gRPC status codes will be retried using an exponential backoff with jitter algorithm as described in the [gRPC Retry Design](https://github.com/grpc/proposal/blob/master/A6-client-retries.md#exponential-backoff).

The policy has the following configuration, which there is currently no way to customize.

- `maxAttempts`: The maximum number of attempts, including the original request. Defaults to `5`.
- `initialBackoff`: The initial backoff duration. Defaults to `1s`
- `maxBackoff`: The maximum backoff duration. Defaults to `5s`.
- `backoffMultiplier` THe backoff multiplier. Defaults to `1.5`.

### Jaeger exporter

The [Jaeger](https://www.jaegertracing.io/docs/1.21/apis/#protobuf-via-grpc-stable) exporter. This exporter uses gRPC for its communications protocol.

| System property                   | Environment variable              | Description                                                                                        |
|-----------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------------|
| otel.traces.exporter=jaeger       | OTEL_TRACES_EXPORTER=jaeger       | Select the Jaeger exporter                                                                         |
| otel.exporter.jaeger.endpoint     | OTEL_EXPORTER_JAEGER_ENDPOINT     | The Jaeger gRPC endpoint to connect to. Default is `http://localhost:14250`.                       |
| otel.exporter.jaeger.timeout      | OTEL_EXPORTER_JAEGER_TIMEOUT      | The maximum waiting time, in milliseconds, allowed to send each batch. Default is `10000`.         |

### Zipkin exporter

The [Zipkin](https://zipkin.io/zipkin-api/) exporter. It sends JSON in [Zipkin format](https://zipkin.io/zipkin-api/#/default/post_spans) to a specified HTTP URL.

| System property               | Environment variable          | Description                                                                                                           |
|-------------------------------|-------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| otel.traces.exporter=zipkin   | OTEL_TRACES_EXPORTER=zipkin   | Select the Zipkin exporter                                                                                            |
| otel.exporter.zipkin.endpoint | OTEL_EXPORTER_ZIPKIN_ENDPOINT | The Zipkin endpoint to connect to. Default is `http://localhost:9411/api/v2/spans`. Currently only HTTP is supported. |

### Prometheus exporter

The [Prometheus](https://github.com/prometheus/docs/blob/master/content/docs/instrumenting/exposition_formats.md) exporter.

| System property                  | Environment variable             | Description                                                                        |
|----------------------------------|----------------------------------|------------------------------------------------------------------------------------|
| otel.metrics.exporter=prometheus | OTEL_METRICS_EXPORTER=prometheus | Select the Prometheus exporter                                                     |
| otel.exporter.prometheus.port    | OTEL_EXPORTER_PROMETHEUS_PORT    | The local port used to bind the prometheus metric server. Default is `9464`.       |
| otel.exporter.prometheus.host    | OTEL_EXPORTER_PROMETHEUS_HOST    | The local address used to bind the prometheus metric server. Default is `0.0.0.0`. |

Note that this is a pull exporter - it opens up a server on the local process listening on the specified host and port, which
a Prometheus server scrapes from.

### Logging exporter

The logging exporter prints the name of the span along with its attributes to stdout. It's mainly used for testing and debugging.

| System property               | Environment variable          | Description                                                          |
|-------------------------------|-------------------------------|----------------------------------------------------------------------|
| otel.traces.exporter=logging  | OTEL_TRACES_EXPORTER=logging  | Select the logging exporter for tracing                              |
| otel.metrics.exporter=logging | OTEL_METRICS_EXPORTER=logging | Select the logging exporter for metrics                              |
| otel.logs.exporter=logging    | OTEL_LOGS_EXPORTER=logging    | Select the logging exporter for logs                                 |

### Logging OTLP JSON exporter

The logging-otlp exporter writes the telemetry data to the JUL logger in OLTP JSON form. It's a more verbose output mainly used for testing and debugging.

| System property                    | Environment variable               | Description                                        |
|------------------------------------|------------------------------------|----------------------------------------------------|
| otel.traces.exporter=logging-otlp  | OTEL_TRACES_EXPORTER=logging-otlp  | Select the logging OTLP JSON exporter for tracing  |
| otel.metrics.exporter=logging-otlp | OTEL_METRICS_EXPORTER=logging-otlp | Select the logging OTLP JSON exporter for metrics  |
| otel.logs.exporter=logging-otlp    | OTEL_LOGS_EXPORTER=logging-otlp    | Select the logging OTLP JSON exporter for logs     |

**NOTE:** While the `OtlpJsonLogging{Signal}Exporters` are stable, specifying their use
via `otel.{signal}.exporter=logging-otlp` is experimental and subject to change or removal.

## Propagator

The propagators determine which distributed tracing header formats are used, and which baggage propagation header formats are used.

| System property  | Environment variable | Description                                                                                                               |
|------------------|----------------------|---------------------------------------------------------------------------------------------------------------------------|
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

| System property                          | Environment variable                     | Description                                                                                                |
|------------------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------|
| otel.resource.attributes                 | OTEL_RESOURCE_ATTRIBUTES                 | Specify resource attributes in the following format: key1=val1,key2=val2,key3=val3                         |
| otel.service.name                        | OTEL_SERVICE_NAME                        | Specify logical service name. Takes precedence over `service.name` defined with `otel.resource.attributes` |
| otel.experimental.resource.disabled-keys | OTEL_EXPERIMENTAL_RESOURCE_DISABLED_KEYS | Specify resource attribute keys that are filtered.                                                         |

You almost always want to specify the [`service.name`](https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/resource/semantic_conventions#service) for your application.
It corresponds to how you describe the application, for example `authservice` could be an application that authenticates requests, and `cats` could be an application that returns information about [cats](https://en.wikipedia.org/wiki/Cat).
You would specify that by setting service name property in one of the following ways:
* directly via `OTEL_SERVICE_NAME=authservice` or `-Dotel.service.name=cats`
* by `service.name` resource attribute like `OTEL_RESOURCE_ATTRIBUTES=service.name=authservice`, or `-Dotel.resource.attributes=service.name=cats,service.namespace=mammals`.

If not specified, SDK defaults the service name to `unknown_service:java`.

### Resource Provider SPI

The [autoconfigure-spi](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure-spi)
SDK extension provides a ResourceProvider SPI that allows libraries to automatically provide
Resources, which are merged into a single Resource by the autoconfiguration module. You can create
your own ResourceProvider, or optionally use an artifact that includes built-in ResourceProviders:

* [io.opentelemetry.instrumentation:opentelemetry-resources](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources)
  includes providers for
  a [predefined set of common resources](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources/library/src/main/java/io/opentelemetry/instrumentation/resources)
* [io.opentelemetry.contrib:opentelemetry-aws-resources](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-resources)
  includes providers
  for [common AWS resources](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-resources/src/main/java/io/opentelemetry/contrib/aws/resource)

### Disabling Automatic ResourceProviders

If you are using the `ResourceProvider` SPI (many instrumentation agent distributions include this automatically),
you can enable / disable one or more of them by using the following configuration items:

| System property                       | Environment variable                  | Description                                                                                 |
|---------------------------------------|---------------------------------------|---------------------------------------------------------------------------------------------|
| otel.java.enabled.resource.providers  | OTEL_JAVA_ENABLED_RESOURCE_PROVIDERS  | Enables one or more `ResourceProvider` types. If unset, all resource providers are enabled. |
| otel.java.disabled.resource.providers | OTEL_JAVA_DISABLED_RESOURCE_PROVIDERS | Disables one or more `ResourceProvider` types                                               |

The value for these properties must be a comma separated list of fully qualified `ResourceProvider` classnames.
For example, if you don't want to expose the name of the operating system through the resource, you
can pass the following JVM argument:

```
-Dotel.java.disabled.resource.providers=io.opentelemetry.instrumentation.resources.OsResourceProvider
```

## Batch span processor

| System property                | Environment variable           | Description                                                                        |
|--------------------------------|--------------------------------|------------------------------------------------------------------------------------|
| otel.bsp.schedule.delay        | OTEL_BSP_SCHEDULE_DELAY        | The interval, in milliseconds, between two consecutive exports. Default is `5000`. |
| otel.bsp.max.queue.size        | OTEL_BSP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                                         |
| otel.bsp.max.export.batch.size | OTEL_BSP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                                          |
| otel.bsp.export.timeout        | OTEL_BSP_EXPORT_TIMEOUT        | The maximum allowed time, in milliseconds, to export data. Default is `30000`.     |

## Sampler

The sampler configures whether spans will be recorded for any call to `SpanBuilder.startSpan`.

| System property         | Environment variable    | Description                                                             |
|-------------------------|-------------------------|-------------------------------------------------------------------------|
| otel.traces.sampler     | OTEL_TRACES_SAMPLER     | The sampler to use for tracing. Defaults to `parentbased_always_on`     |
| otel.traces.sampler.arg | OTEL_TRACES_SAMPLER_ARG | An argument to the configured tracer if supported, for example a ratio. |

Supported values for `otel.traces.sampler` are

- "always_on": AlwaysOnSampler
- "always_off": AlwaysOffSampler
- "traceidratio": TraceIdRatioBased. `otel.traces.sampler.arg` sets the ratio.
- "parentbased_always_on": ParentBased(root=AlwaysOnSampler)
- "parentbased_always_off": ParentBased(root=AlwaysOffSampler)
- "parentbased_traceidratio": ParentBased(root=TraceIdRatioBased). `otel.traces.sampler.arg` sets the ratio.

## Attribute limits

These properties can be used to control the maximum number and length of attributes.

| System property                   | Environment variable              | Description                                                                                              |
|-----------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------------------|
| otel.attribute.value.length.limit | OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT | The maximum length of attribute values. Applies to spans and logs. By default there is no limit.         |
| otel.attribute.count.limit        | OTEL_ATTRIBUTE_COUNT_LIMIT        | The maximum number of attributes. Applies to spans, span events, span links, and logs. Default is `128`. |

## Span limits

These properties can be used to control the maximum size of spans by placing limits on attributes, events, and links.

| System property                        | Environment variable                   | Description                                                                                                                           |
|----------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| otel.span.attribute.value.length.limit | OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT | The maximum length of span attribute values. Takes precedence over `otel.attribute.value.length.limit`. By default there is no limit. |
| otel.span.attribute.count.limit        | OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT        | The maximum number of attributes per span. Takes precedence over `otel.attribute.count.limit`. Default is `128`.                      |
| otel.span.event.count.limit            | OTEL_SPAN_EVENT_COUNT_LIMIT            | The maximum number of events per span. Default is `128`.                                                                              |
| otel.span.link.count.limit             | OTEL_SPAN_LINK_COUNT_LIMIT             | The maximum number of links per span. Default is `128`                                                                                |

## Exemplars

| System property              | Environment variable         | Description                                                                                                     |
|------------------------------|------------------------------|-----------------------------------------------------------------------------------------------------------------|
| otel.metrics.exemplar.filter | OTEL_METRICS_EXEMPLAR_FILTER | The filter for exemplar sampling.  Can be `ALWAYS_OFF`, `ALWAYS_ON` or `TRACE_BASED`. Default is `TRACE_BASED`. |

## Periodic Metric Reader

| System property             | Environment variable        | Description                                                                                  |
|-----------------------------|-----------------------------|----------------------------------------------------------------------------------------------|
| otel.metric.export.interval | OTEL_METRIC_EXPORT_INTERVAL | The interval, in milliseconds, between the start of two export attempts. Default is `60000`. |

## Batch log record processor

| System property                 | Environment variable            | Description                                                                        |
|---------------------------------|---------------------------------|------------------------------------------------------------------------------------|
| otel.blrp.schedule.delay        | OTEL_BLRP_SCHEDULE_DELAY        | The interval, in milliseconds, between two consecutive exports. Default is `5000`. |
| otel.blrp.max.queue.size        | OTEL_BLRP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                                         |
| otel.blrp.max.export.batch.size | OTEL_BLRP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                                          |
| otel.blrp.export.timeout        | OTEL_BLRP_EXPORT_TIMEOUT        | The maximum allowed time, in milliseconds, to export data. Default is `30000`.     |

## Customizing the OpenTelemetry SDK

Autoconfiguration exposes SPI [hooks](../autoconfigure-spi/src/main/java/io/opentelemetry/sdk/autoconfigure/spi) for customizing behavior programmatically as needed.
It's recommended to use the above configuration properties where possible, only implementing the SPI to add functionality not found in the
SDK by default.
