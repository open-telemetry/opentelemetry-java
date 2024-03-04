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

public class InstrumentGarbageCollectionBenchmarkTest {

  /**
   * This test validates that in {@link MemoryMode#REUSABLE_DATA}, any {@link
   * MetricStorage#collect(Resource, InstrumentationScopeInfo, long, long)} barely allocates memory
   * which is then subsequently garbage collected. It is done so comparatively to {@link
   * MemoryMode#IMMUTABLE_DATA},
   *
   * <p>It runs the JMH test {@link InstrumentGarbageCollectionBenchmark} with GC profiler, and
   * measures for each parameter combination the garbage collector normalized rate (bytes allocated
   * per Operation).
   *
   * <p>Memory allocations can be hidden even at an innocent foreach loop on a collection, which
   * under the hood allocates an internal object O(N) times. Someone can accidentally refactor such
   * loop, resulting in 30% increase of garbage collected objects during a single collect() run.
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void normalizedAllocationRateTest() throws RunnerException {
    // OTel GitHub CI Workflow (see .github/) sets an environment variable
    // (RUN_JMH_BASED_TESTS=true).
    // We set it only there since it's a lengthy test (roughly 2.5min)
    // and we want to run it only in CI.
    Assumptions.assumeTrue(
        "true".equals(System.getenv("RUN_JMH_BASED_TESTS")),
        "This test should only run in GitHub CI since it's long");

    // Runs InstrumentGarbageCollectionBenchmark
    // with garbage collection profiler
    Options opt =
        new OptionsBuilder()
            .include(InstrumentGarbageCollectionBenchmark.class.getSimpleName())
            .addProfiler("gc")
            .shouldFailOnError(true)
            .jvmArgs("-Xmx1500m")
            .build();
    Collection<RunResult> results = new Runner(opt).run();

    // Collect the normalized GC allocation rate per parameters combination
    Map<String, TestInstrumentTypeResults> testInstrumentTypeResultsMap = new HashMap<>();
    for (RunResult result : results) {
      for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
        BenchmarkParams benchmarkParams = benchmarkResult.getParams();

        String memoryMode = benchmarkParams.getParam("memoryMode");
        String aggregationTemporality = benchmarkParams.getParam("aggregationTemporality");
        String testInstrumentType = benchmarkParams.getParam("testInstrumentType");
        assertThat(memoryMode).isNotNull();
        assertThat(aggregationTemporality).isNotNull();
        assertThat(testInstrumentType).isNotNull();

        Map<String, Result> secondaryResults = benchmarkResult.getSecondaryResults();
        Result allocRateNorm = secondaryResults.get("gc.alloc.rate.norm");
        assertThat(allocRateNorm)
            .describedAs("Allocation rate in secondary results: %s", secondaryResults)
            .isNotNull();

        testInstrumentTypeResultsMap
            .computeIfAbsent(testInstrumentType, k -> new TestInstrumentTypeResults())
            .aggregationTemporalityToMemoryModeResult
            .computeIfAbsent(aggregationTemporality, k -> new HashMap<>())
            .put(memoryMode, allocRateNorm.getScore());
      }
    }

    testInstrumentTypeResultsMap.forEach(
        (testInstrumentTypeString, testInstrumentTypeResults) -> {
          Map<String, Map<String, Double>> resultMap =
              testInstrumentTypeResults.aggregationTemporalityToMemoryModeResult;
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

                TestInstrumentType testInstrumentType =
                    TestInstrumentType.valueOf(testInstrumentTypeString);
                float dataAllocRateReductionPercentage =
                    testInstrumentType.getDataAllocRateReductionPercentage();
                double allowedOffset = testInstrumentType.getAllowedPercentOffset();

                // If this test suddenly fails for you this means you have changed the code in a way
                // that allocates more memory than before. You can find out where, by running
                // ProfileBenchmark class and looking at the flame graph. Make sure to
                // set the parameters according to where it failed for.
                assertThat(100 - (reusableDataAllocRate / immutableDataAllocRate) * 100)
                    .describedAs(
                        "Aggregation temporality = %s, testInstrumentType = %s",
                        aggregationTemporality, testInstrumentTypeString)
                    .isCloseTo(dataAllocRateReductionPercentage, Offset.offset(allowedOffset));
              });
        });
  }

  static class TestInstrumentTypeResults {
    Map<String, Map<String, Double>> aggregationTemporalityToMemoryModeResult = new HashMap<>();
  }
}
