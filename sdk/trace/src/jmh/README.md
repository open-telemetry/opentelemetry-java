# Batch span processor benchmarks

These benchmarks create and end sampled spans through an `SdkTracerProvider` with a
`BatchSpanProcessor`. They share an iteration-scoped provider and metric reader, drain the queue
before collecting counters, and close their resources between iterations. Queue-full logging is
disabled during a trial and restored afterward. The default exporter is a synchronous no-op; `-p
delayMs=1` selects the asynchronous delaying exporter instead.

|                  Class                   |                             Workload                              |                                 Primary result                                 |
|------------------------------------------|-------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `BatchSpanProcessorBenchmark`            | Five producers, bursts of `spanCount` spans followed by a flush   | Bursts/s, including span creation and flush                                    |
| `BatchSpanProcessorMultiThreadBenchmark` | Unpaced producers at several thread counts                        | Attempts/s, including span creation and dropped spans                          |
| `BatchSpanProcessorCpuBenchmark`         | Producers park for 100 microseconds after each span               | Paced operations/s; use the CPU counters or an external profiler to assess CPU |
| `BatchSpanProcessorSpanEndBenchmark`     | Deadline-paced producers; creation and pacing precede each sample | Sampled public `Span.end()` duration in ns                                     |

The former `BatchSpanProcessorFlushBenchmark` duplicated the burst-and-flush workload;
`BatchSpanProcessorDroppedSpansBenchmark` duplicated the five-producer unpaced workload. Use the
corresponding entries above.

## Accounting

Each iteration prints a `BSP_LEDGER` record **after** timing and the final drain. It checks
`attempted = exported + dropped`. The export count comes from the SDK's `processedSpans` metric: it
counts spans submitted to the exporter, not downstream delivery acknowledgments. For the successful
no-op exporter, this is also the completed export count.

Throughput benchmarks also expose raw `attemptedSpans`, `exportedSpans`, `droppedSpans`,
`elapsedNanos`, and `exporterCpuNanos` auxiliary counters in JMH JSON. Shared totals are published
by one producer only, so aggregation across threads, iterations, and forks preserves counts. Compute
ratios **after** aggregating:

- Drop fraction: `sum(droppedSpans) / sum(attemptedSpans)`.
- Complete-iteration export rate: `1e9 * sum(exportedSpans) / sum(elapsedNanos)`.
- Worker CPU fraction of one core: `sum(exporterCpuNanos) / sum(elapsedNanos)`.

The complete-iteration clock includes synchronization calls around JMH's measured loop and the final
queue drain. Do not divide these counters by the primary result's measured duration or expect their
attempt count to match JMH's measured operation count. `exporterCpuNanos` is the SDK worker thread's
lifetime CPU before shutdown; it excludes producers and asynchronous exporter executor threads.
Missing or disabled thread CPU accounting is reported as `NaN`.

Latency runs use stdout ledgers because JMH does not support these auxiliary counters in sample time
mode. Their `skippedSlots` counts arrival slots missed because a producer was scheduled late; missed
slots are skipped rather than replayed as a catch-up burst. Check achieved load and skipped slots
when comparing tails. This is a paced, closed-loop measurement of executed calls, not an open-loop
event-loop-lag test. It does not compensate for calls that could not start on time.

Latency runs fail if the queue drops any spans, or if JMH's iteration batch size is not one.
Invocation fixtures and timestamp overhead limit interpretation of sub-microsecond durations.
Sampled percentiles are observations, not a worst-case bound or a guarantee against native mutex
contention. Keep per-fork results and inspect their spread rather than averaging percentiles.

## Running a comparison

Build with the repository's required JDK. Run JMH with the same runtime for the runner and forks;
when comparing revisions, keep the benchmark sources and parameters identical. Use separate result
files, alternate revision order, and keep independent forks. Do not run builds or other benchmarks
concurrently.

```sh
./gradlew :sdk:trace:jmhJar
java -Djmh.blackhole.autoDetect=false \
  -jar sdk/trace/build/libs/opentelemetry-sdk-trace-*-jmh.jar \
  '^io\.opentelemetry\.sdk\.trace\.export\.BatchSpanProcessor(MultiThreadBenchmark\.export_(05|10)Thread|SpanEndBenchmark\.end_(01|10)Thread)$' \
  -f 3 -wi 3 -w 2s -i 5 -r 2s -prof gc -rf json -rff results.json
```

A normal 512-span batch amortizes worker notifications. To exercise the frequent-wakeup case, select
the single-producer latency method with `-p maxExportBatchSize=1`. The default latency target is
10,000 spans/s **per producer**; override it with `-p targetSpansPerSecond=5000`, and check the
ledger rather than assuming the target was achieved. `maxQueueSize` and `maxExportBatchSize` are
also explicit JMH parameters, defaulting to 2048 and 512.
