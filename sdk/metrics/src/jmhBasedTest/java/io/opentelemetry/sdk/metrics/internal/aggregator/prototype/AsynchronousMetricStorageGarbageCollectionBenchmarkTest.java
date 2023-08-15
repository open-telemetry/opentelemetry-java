package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import static io.opentelemetry.sdk.metrics.internal.aggregator.prototype.AsynchronousMetricStorageGarbageCollectionBenchmark.Filter.NO_FILTER;
import static io.opentelemetry.sdk.metrics.internal.aggregator.prototype.AsynchronousMetricStorageGarbageCollectionBenchmark.Filter.WITH_FILTER;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

  @SuppressWarnings({"rawtypes", "SystemOut"})
  @Test
  public void normalizedAllocationRateTest() throws RunnerException {
    // Runs AsynchronousMetricStorageMemoryProfilingBenchmark
    // with garbage collection profiler
    Options opt =
        new OptionsBuilder()
            .include(AsynchronousMetricStorageGarbageCollectionBenchmark.class.getSimpleName())
            .addProfiler("gc")
            .shouldFailOnError(true)
            .build();
    Collection<RunResult> results = new Runner(opt).run();

    // Collect the normalized GC allocation rate per parameters combination
    Map<String, Map<String, Double>> resultMap = new HashMap<>();
    for (RunResult result : results) {
      for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
        BenchmarkParams benchmarkParams = benchmarkResult.getParams();

        String filter = benchmarkParams.getParam("filter");
        String aggregationTemporality = benchmarkParams.getParam("aggregationTemporality");
        assertThat(filter).isNotNull();
        assertThat(aggregationTemporality).isNotNull();

        Map<String, Result> secondaryResults = benchmarkResult.getSecondaryResults();
        Result allocRateNorm = secondaryResults.get("gc.alloc.rate.norm");
        assertThat(allocRateNorm)
            .describedAs("Allocation rate in secondary results: %s", secondaryResults)
            .isNotNull();

        resultMap
            .computeIfAbsent(aggregationTemporality, k -> new HashMap<>())
            .put(filter, allocRateNorm.getScore());
      }
    }

    assertThat(resultMap).hasSameSizeAs(AggregationTemporality.values());

    resultMap.forEach(
        (aggregationTemporality, memoryModeToAllocRateMap) -> {
          Double withoutFilterDataAllocRate =
              memoryModeToAllocRateMap.get(NO_FILTER.toString());
          Double withFilterDataAllocRate =
              memoryModeToAllocRateMap.get(WITH_FILTER.toString());

          assertThat(withoutFilterDataAllocRate).isNotNull().isNotZero();
          assertThat(withFilterDataAllocRate).isNotNull().isNotZero();
          System.out.printf("\n%s: without filter = %,15.0f",
              aggregationTemporality,
              withoutFilterDataAllocRate);
          System.out.printf("\n%s: with filter = %,15.2f\n",
              aggregationTemporality,
              withFilterDataAllocRate);
          System.out.printf("\n%s: with filter saves %.2f%%\n",
              aggregationTemporality,
              100 - (withFilterDataAllocRate / withoutFilterDataAllocRate) * 100);

//          assertThat(100 - (withFilterDataAllocRate / withoutFilterDataAllocRate) * 100)
//              .isCloseTo(99.8, Offset.offset(2.0));
        });
  }
}
