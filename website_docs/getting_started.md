---
title: "Getting Started"
weight: 2
---

## Automatic Instrumentation

> Automatic instrumentation is applicable to trace data only today.

Download the [latest version](/docs/java/#releases-1). This package includes
the instrumentation agent, instrumentations for all supported libraries and all
available data exporters.

The instrumentation agent is enabled by passing the `-javaagent` flag to the JVM.

```java
java -javaagent:path/to/opentelemetry-javaagent-all.jar \
     -jar myapp.jar
```

By default OpenTelemetry Java agent uses the [OTLP
exporter](https://github.com/open-telemetry/opentelemetry-java/tree/main/exporters/otlp)
configured to send data to a local [OpenTelemetry
Collector](https://github.com/open-telemetry/opentelemetry-collector/blob/main/receiver/otlpreceiver/README.md)
at `localhost:55680`.

Configuration parameters are passed as Java system properties (`-D` flags) or as
environment variables. For example:

```java
java -javaagent:path/to/opentelemetry-javaagent-all.jar \
     -Dotel.exporter=zipkin \
     -jar myapp.jar
```

An exporter jar can be specified via the `otel.exporter.jar` system property:

```java
java -javaagent:path/to/opentelemetry-javaagent-all.jar \
     -Dotel.exporter.jar=path/to/external-exporter.jar \
     -jar myapp.jar
```

For additional information, see the [Automatic Instrumentation documentation](/docs/java/automatic_instrumentation).

## Manual Instrumentation

Please see the [QuickStart Guide](https://github.com/open-telemetry/opentelemetry-java/blob/v1.0.0/QUICKSTART.md).

#### Example

Several
[examples](https://github.com/open-telemetry/opentelemetry-java/tree/main/examples)
are provided to get started. The [metrics
examples](https://github.com/open-telemetry/opentelemetry-java/tree/main/examples/metrics)
demonstrates how to generate other types of metrics.
