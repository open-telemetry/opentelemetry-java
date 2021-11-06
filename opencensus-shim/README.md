# OpenTelemetry OpenCensus Shim

[![Javadocs][javadoc-image]][javadoc-url]

The OpenCensus shim allows applications and libraries that are instrumented
with OpenTelemetry, but depend on other libraries instrumented with OpenCensus,
to export trace spans from both OpenTelemetry and OpenCensus with the correct
parent-child relationship. It also allows OpenCensus metrics to be exported by
any configured OpenTelemetry metric exporter.

## Usage

### Traces

To allow the shim to work for traces, add the shim as a dependency.

Nothing else needs to be added to libraries for this to work.

Applications only need to set up OpenTelemetry exporters, not OpenCensus.

### Metrics

To allow the shim to work for metrics, add the shim as a dependency.

Applications also need to attach OpenCensus metrics to their metric readers on registration.

```
SdkMeterProvider.builder()
            .registerMetricReader(
                OpenCensusMetrics.attachTo(readerFactory)
            )
        .buildAndRegisterGlobal();
```

For example, if a logging exporter were configured, the following would be
added:

```
LoggingMetricExporter metricExporter = new LoggingMetricExporter();
SdkMeterProvider.builder()
            .registerMetricReader(
                OpenCensusMetrics.attachTo(
                  PeriodicMetricReader.builder(metricExporter)
                  .newMetricReaderFactory()
                )
            )
        .buildAndRegisterGlobal();
```

## Known Problems

* OpenCensus links added after an OpenCensus span is created will not be
exported, as OpenTelemetry only supports links added when a span is created.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opencensus-shim.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opencensus-shim
