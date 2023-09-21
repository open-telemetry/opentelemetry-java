/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class AsynchronousMetricStorageGarbageCollectionBenchmarkTest {

  /**
   * This test validates that in {@link MemoryMode#REUSABLE_DATA}, {@link
   * AsynchronousMetricStorage#collect(Resource, InstrumentationScopeInfo, long, long)} barely
   * allocates memory which is then subsequently garbage collected. It is done so comparatively to
   * {@link MemoryMode#IMMUTABLE_DATA},
   *
   * <p>It runs the JMH test {@link AsynchronousMetricStorageGarbageCollectionBenchmark} with GC
   * profiler, and measures for each parameter combination the garbage collector normalized rate
   * (bytes allocated per Operation).
   *
   * <p>Memory allocations can be hidden even at an innocent foreach loop on a collection, which
   * under the hood allocates an internal object O(N) times. Someone can accidentally refactor such
   * loop, resulting in 30% increase of garbage collected objects during a single collect() run.
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void normalizedAllocationRateTest() throws RunnerException {
    // GitHub CI has an environment variable (CI=true). We can use it to skip
    // this test since it's a lengthy one (roughly 10 seconds) and have it running
    // only in GitHub CI
    Assumptions.assumeTrue(
        "true".equals(System.getenv("CI")),
        "This test should only run in GitHub CI since it's long");

    // Runs AsynchronousMetricStorageMemoryProfilingBenchmark
    // with garbage collection profiler
    Options opt =
        new OptionsBuilder()
            .include(AsynchronousMetricStorageGarbageCollectionBenchmark.class.getSimpleName())
            .addProfiler("gc")
            .shouldFailOnError(true)
            .jvmArgs("-Xmx1500m")
            .build();
    Collection<RunResult> results = new Runner(opt).run();

    // Collect the normalized GC allocation rate per parameters combination
    Map<String, Map<String, Double>> resultMap = new HashMap<>();
    for (RunResult result : results) {
      for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
        BenchmarkParams benchmarkParams = benchmarkResult.getParams();

        String memoryMode = benchmarkParams.getParam("memoryMode");
        String aggregationTemporality = benchmarkParams.getParam("aggregationTemporality");
        assertThat(memoryMode).isNotNull();
        assertThat(aggregationTemporality).isNotNull();

        Map<String, Result> secondaryResults = benchmarkResult.getSecondaryResults();
        Result allocRateNorm = secondaryResults.get("gc.alloc.rate.norm");
        assertThat(allocRateNorm)
            .describedAs("Allocation rate in secondary results: %s", secondaryResults)
            .isNotNull();

        resultMap
            .computeIfAbsent(aggregationTemporality, k -> new HashMap<>())
            .put(memoryMode, allocRateNorm.getScore());
      }
    }

    assertThat(resultMap).hasSameSizeAs(AggregationTemporality.values());

    // Asserts that reusable data GC allocation rate is a tiny fraction of immutable data
    // GC allocation rate
    resultMap.forEach(
        (aggregationTemporality, memoryModeToAllocRateMap) -> {
          Double immutableDataAllocRate =
              memoryModeToAllocRateMap.get(MemoryMode.IMMUTABLE_DATA.toString());
          Double reusableDataAllocRate =
              memoryModeToAllocRateMap.get(MemoryMode.REUSABLE_DATA.toString());

          assertThat(immutableDataAllocRate).isNotNull().isNotZero();
          assertThat(reusableDataAllocRate).isNotNull().isNotZero();
          assertThat(100 - (reusableDataAllocRate / immutableDataAllocRate) * 100)
              .isCloseTo(99.8, Offset.offset(2.0));
        });
  }
}
