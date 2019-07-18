# OpenTelemetry SDK Contrib Async Processor

An implementation of the trace `SpanProcessors` that uses
[Disruptor](https://github.com/LMAX-Exchange/disruptor) to make all the `SpanProcessors` hooks run
async.

* Java 8 compatible.
