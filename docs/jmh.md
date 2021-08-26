
# how to jmh

[jmh] (Java Benchmark Harness) is a tool for running benchmarks and reporting results.

opentelemetry-java has a lot of micro benchmarks. They live inside
`jmh` directories in the appropriate module.

The benchmarks are run with a gradle plugin.

To run an entire suite for a module, you can run the jmh gradle task.
As an example, here's how you can run the benchmarks for all of
the sdk trace module.

```
`./gradlew :sdk:trace:jmh`
```

If you just want to run a single benchmark and not the entire suite:

`./gradlew -PjmhIncludeSingleClass=BatchSpanProcessorBenchmark :sdk:trace:jmh`
