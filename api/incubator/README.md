# API Incubator

Experimental APIs, including Event API, extended Log Bridge APIs, extended Metrics APIs, extended ContextPropagator APIs, and extended Trace APIs.

## Event API

Features:

* Event API for producing log records according to [Event Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/general/events/).

See [EventApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/events/EventApiUsageTest.java).

## Extended Log Bridge API

Features:

* Check if logger is enabled before emitting logs to avoid unnecessary computation

See [ExtendedLogsBridgeApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/logs/ExtendedLogsBridgeApiUsageTest.java).

## Extended Metrics APIs

Features:

* Attributes advice

See [ExtendedMetricsApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/metrics/ExtendedMetricsApiUsageTest.java).

## Extended ContextPropagator APIs

Features:

* Check if instrument is enabled before recording measurements to avoid unnecessary computation
* Simplified injection / extraction of context

See [ExtendedContextPropagatorsUsageTest](./src/test/java/io/opentelemetry/api/incubator/propagation/ExtendedContextPropagatorsUsageTest.java).

## Extended Trace APIs

Features:

* Check if tracer is enabled before starting spans to avoid unnecessary computation
* Utility methods to reduce boilerplace using span API, including extracting context, and wrapping runnables / callables with spans

See [ExtendedTraceApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/trace/ExtendedTraceApiUsageTest.java).
