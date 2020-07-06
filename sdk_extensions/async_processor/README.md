# OpenTelemetry SDK Contrib Async Processor

[![Javadocs][javadoc-image]][javadoc-url]

An implementation of the trace `SpanProcessors` that uses
[Disruptor](https://github.com/LMAX-Exchange/disruptor) to make all the `SpanProcessors` hooks run
async.

* Java 8 compatible.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-contrib-async-processor.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-contrib-async-processor