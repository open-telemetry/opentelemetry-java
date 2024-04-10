/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;

/**
 * This benchmark class is used to see memory allocation flame graphs for a single run.
 *
 * <p>Steps:
 *
 * <ol>
 *   <li>Follow download instructions for async-profiler, located at <a
 *       href="https://github.com/async-profiler/async-profiler">this location</a>
 *   <li>Assuming you have extracted it at /tmp/async-profiler-2.9-macos, add the following to your
 *       JVM arguments of your run configuration:
 *       <pre>
 *       -agentpath:/tmp/async-profiler-2.9-macos/build/libasyncProfiler.so=start,event=alloc,flamegraph,file=/tmp/profiled_data.html
 *       </pre>
 *   <li>Tune the parameters as you see fit (They are marked below with "Parameters")
 *   <li>Run the class (its main function)
 *   <li>Open /tmp/profiled_data.html with your browser
 *   <li>Use the flame graph to see where the allocations are happening the most and fix
 *   <li>Run {@link InstrumentGarbageCollectionBenchmark} and see if it passes now
 *   <li>If not, repeat
 * </ol>
 */
public class ProfileBenchmark {

  private ProfileBenchmark() {}

  public static void main(String[] args) {
    // Parameters
    AggregationTemporality aggregationTemporality = AggregationTemporality.DELTA;
    MemoryMode memoryMode = MemoryMode.REUSABLE_DATA;
    TestInstrumentType testInstrumentType = TestInstrumentType.DOUBLE_LAST_VALUE;

    InstrumentGarbageCollectionBenchmark.ThreadState benchmarkSetup =
        new InstrumentGarbageCollectionBenchmark.ThreadState();

    benchmarkSetup.aggregationTemporality = aggregationTemporality;
    benchmarkSetup.memoryMode = memoryMode;
    benchmarkSetup.testInstrumentType = testInstrumentType;

    InstrumentGarbageCollectionBenchmark benchmark = new InstrumentGarbageCollectionBenchmark();

    benchmarkSetup.setup();

    warmup(benchmark, benchmarkSetup);

    // This is divided explicitly to two methods so you can focus on `measure` in the flame graph
    // when trying to decrease the allocations
    measure(benchmark, benchmarkSetup);
  }

  public static void warmup(
      InstrumentGarbageCollectionBenchmark benchmark,
      InstrumentGarbageCollectionBenchmark.ThreadState benchmarkSetup) {
    for (int i = 0; i < 10; i++) {
      benchmark.recordAndCollect(benchmarkSetup);
    }
  }

  public static void measure(
      InstrumentGarbageCollectionBenchmark benchmark,
      InstrumentGarbageCollectionBenchmark.ThreadState benchmarkSetup) {
    for (int i = 0; i < 200; i++) {
      benchmark.recordAndCollect(benchmarkSetup);
    }
  }
}
