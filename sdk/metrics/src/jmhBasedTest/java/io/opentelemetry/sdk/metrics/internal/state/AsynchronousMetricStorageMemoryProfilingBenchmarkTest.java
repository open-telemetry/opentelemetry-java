package io.opentelemetry.sdk.metrics.internal.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.opentelemetry.sdk.metrics.export.MemoryMode;
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

public class AsynchronousMetricStorageMemoryProfilingBenchmarkTest {
    private static final Logger logger = Logger.getLogger(
            AsynchronousMetricStorageMemoryProfilingBenchmarkTest.class.getName());

    @SuppressWarnings({"rawtypes", "SystemOut"})
    @Test
    public void testMe() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AsynchronousMetricStorageMemoryProfilingBenchmark.class.getSimpleName())
                .addProfiler("gc")
                .shouldFailOnError(true)
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        for (RunResult result : results) {
            for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
                // Extract the benchmark parameters
                BenchmarkParams benchmarkParams = benchmarkResult.getParams();

                // Extract memoryMode and aggregationTemporality from benchmarkParams
                String memoryMode = benchmarkParams.getParam("memoryMode");
                String aggregationTemporality = benchmarkParams.getParam("aggregationTemporality");

                // Extract secondary results
                Map<String, Result> secondaryResults = benchmarkResult.getSecondaryResults();

                // Extract the gc.alloc.rate.norm metric
                Result allocRateNorm = secondaryResults.get("·gc.alloc.rate.norm");

                if (allocRateNorm != null) {
                    System.out.println("Memory Mode: " + memoryMode +
                            ", Aggregation Temporality: " + aggregationTemporality +
                            ", gc.alloc.rate.norm: " + allocRateNorm.getScore());
                } else {
                    System.out.println("Memory Mode: " + memoryMode +
                            ", Aggregation Temporality: " + aggregationTemporality +
                            ", gc.alloc.rate.norm not found");
                }

                if (allocRateNorm != null) {
                    resultMap
                            .computeIfAbsent(aggregationTemporality, k -> new HashMap<>())
                            .put(memoryMode, allocRateNorm.getScore());
                }
            }

            logger.info("Hello there");
        }

        resultMap.forEach((aggregationTemporality, memoryModeToAllocRateMap) -> {
            Double immutableDataAllocRate = memoryModeToAllocRateMap.get(MemoryMode.IMMUTABLE_DATA.toString());
            Double reusableDataAllocRate = memoryModeToAllocRateMap.get(MemoryMode.REUSABLE_DATA.toString());

            assertThat(reusableDataAllocRate).is
        });


    }
}
