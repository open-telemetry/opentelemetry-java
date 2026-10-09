# API Incubator

Experimental APIs, including extended Log Bridge APIs, extended Metrics APIs, extended ContextPropagator APIs, and extended Trace APIs.

## Extended Log Bridge API

Features:

* Add extended attributes to log records to encode complex data structures

See [ExtendedLogsBridgeApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/logs/ExtendedLogsBridgeApiUsageTest.java).

## Extended Metrics APIs

Features:

* Attributes advice
* Bound instruments

See [ExtendedMetricsApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/metrics/ExtendedMetricsApiUsageTest.java), [BoundInstrumentUsageTest](./src/test/java/io/opentelemetry/api/incubator/metrics/BoundInstrumentUsageTest.java).

## Extended ContextPropagator APIs

Features:

* Simplified injection / extraction of context

See [ExtendedContextPropagatorsUsageTest](./src/test/java/io/opentelemetry/api/incubator/propagation/ExtendedContextPropagatorsUsageTest.java).

## Extended Trace APIs

Features:

* Utility methods to reduce boilerplate using span API, including extracting context, and wrapping runnables / callables with spans

See [ExtendedTraceApiUsageTest](./src/test/java/io/opentelemetry/api/incubator/trace/ExtendedTraceApiUsageTest.java).
