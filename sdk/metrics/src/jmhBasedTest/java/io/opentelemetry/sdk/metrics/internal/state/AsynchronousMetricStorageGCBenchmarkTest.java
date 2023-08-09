package io.opentelemetry.sdk.metrics.internal.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AsynchronousMetricStorageGCBenchmarkTest {

    /*
     * This test validates that in REUSABLE_DATA memory mode, collect()
     * of AsynchronousMetricStorage barely allocates memory which is
     * then subsequently garbage collected.
     *
     * Memory allocations can be hidden even at an innocent foreach loop on a collection,
     * which under the hood allocates an internal object O(N) times. Someone can
     * accidentally refactor such loop, resulting in 30% increase of garbage collected
     * objects during a single collect() run.
     *
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void normalizedAllocationRateTest() throws RunnerException {
        // Runs AsynchronousMetricStorageMemoryProfilingBenchmark
        // with garbage collection profiler
        Options opt = new OptionsBuilder()
                .include(AsynchronousMetricStorageGCBenchmark.class.getSimpleName())
                .addProfiler("gc")
                .shouldFailOnError(true)
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
                Result allocRateNorm = secondaryResults.get("·gc.alloc.rate.norm");

                if (allocRateNorm != null) {
                    resultMap.computeIfAbsent(aggregationTemporality, k -> new HashMap<>())
                            .put(memoryMode, allocRateNorm.getScore());
                }
            }
        }

        assertThat(resultMap.size()).isEqualTo(AggregationTemporality.values().length);

        // Asserts that reusable data GC allocation rate is a tiny fraction of immutable data
        // GC allocation rate
        resultMap.forEach((aggregationTemporality, memoryModeToAllocRateMap) -> {
            Double immutableDataAllocRate = memoryModeToAllocRateMap.get(MemoryMode.IMMUTABLE_DATA.toString());
            Double reusableDataAllocRate = memoryModeToAllocRateMap.get(MemoryMode.REUSABLE_DATA.toString());

            assertThat(immutableDataAllocRate).isNotNull().isNotZero();
            assertThat(reusableDataAllocRate).isNotNull().isNotZero();
            assertThat(100 - (reusableDataAllocRate / immutableDataAllocRate) * 100)
                    .isCloseTo(99.8, Offset.offset(2.0));
        });
    }
}
