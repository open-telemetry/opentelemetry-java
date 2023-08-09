/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import org.openjdk.jol.info.GraphLayout;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Fail.fail;


/**
 * Benchmarks the memory usage of different memory modes and aggregation temporalities
 *
 * This benchmark helps to see the difference in heap usage between the memory modes.
 * You can change the aggregation temporality, number of counters and cardinality (attribute sets count).
 * You have to run it manually for each parameter, since it's not JMH-based, and we need a clean heap before we start.
 *
 * Since it uses JOL to run the measurement, it requires you to add the following to JVM arguments
 * of your Run configuration:
 *
 * -Djdk.attach.allowAttachSelf -Djol.magicFieldOffset=true
 *
 * This library has additional usage: You can use it to see memory allocation frame graphs for a single run.
 * Steps:
 * 1. Follow download instructions for async-profiler, located at https://github.com/async-profiler/async-profiler
 * 2. Assuming you have extracted it at /tmp/async-profiler-2.9-macos, add the following to your JVM arguments
 * of your run configuration:
 *
 *  -agentpath:/tmp/async-profiler-2.9-macos/build/libasyncProfiler.so=start,event=alloc,flamegraph,file=/tmp/profiled_data.html
 *
 * 3. Tune the parameters as you see fit
 * 4. Be sure to set skipMemoryUsageMeasurementUsingJol to true (don't want its JOL memory allocations counted)
 * 5. Run the class
 * 6. Open /tmp/profiled_data.html with your browser
 */
@SuppressWarnings("SystemOut")
public class AsynchronousCounterMemoryUsageBenchmark {

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    AtomicLong maxUsedMemory = new AtomicLong(0);

    private AsynchronousCounterMemoryUsageBenchmark() {
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        AsynchronousCounterMemoryUsageBenchmark asynchronousCounterMemoryUsageBenchmark = new AsynchronousCounterMemoryUsageBenchmark();
        asynchronousCounterMemoryUsageBenchmark.measure();
    }

    public void measure() throws ExecutionException, InterruptedException, TimeoutException {
        // Parameters
        AggregationTemporality aggregationTemporality = AggregationTemporality.DELTA;
        MemoryMode memoryMode = MemoryMode.IMMUTABLE_DATA;
        int countersCount = 50;
        int cardinality = 100_000;
        boolean skipMemoryUsageMeasurementUsingJol = false;

        AsynchronousMetricStorageGCBenchmark.ThreadState benchmarkSetup =
                new AsynchronousMetricStorageGCBenchmark.ThreadState(countersCount, cardinality);

        benchmarkSetup.aggregationTemporality = aggregationTemporality;
        benchmarkSetup.memoryMode = memoryMode;

        AsynchronousMetricStorageGCBenchmark benchmark = new AsynchronousMetricStorageGCBenchmark();

        benchmarkSetup.setup();

        waitForGC();
        long usedMemoryBefore = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();

        ScheduledFuture<?> scheduledFuture = startMeasuringUsedMemoryInBackground();
        try {
            runBenchmark(benchmark, benchmarkSetup);
        } finally {
            executorService.shutdown();
        }

        try {
            scheduledFuture.get(10, TimeUnit.SECONDS);
        } catch (CancellationException e) {
            // Due to the shutdown, ignore
        } catch (Throwable t) {
            throw t;
        }

        waitForGC();
        long usedMemoryAfter = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        long memoryUsedButFreed = maxUsedMemory.get() - usedMemoryAfter;

        double sdkMeterProviderSizeOnHeap;
        double attributesListSizeOnHeap;
        if (skipMemoryUsageMeasurementUsingJol) {
            sdkMeterProviderSizeOnHeap = -1;
            attributesListSizeOnHeap = -1;
        } else {
            GraphLayout graphLayout = GraphLayout.parseInstance(benchmarkSetup.sdkMeterProvider);
            GraphLayout graphLayoutAttributes = GraphLayout.parseInstance(benchmarkSetup.attributesList);
            sdkMeterProviderSizeOnHeap = graphLayout.totalSize();
            attributesListSizeOnHeap = graphLayoutAttributes.totalSize();
        }

        System.out.println(String.format("" +
                        "\nCounters = %d, Cardinality = %,d" +
                        "\n%s, %s: " +
                        "\nAttributes memory usage            = %,15.0f [bytes]\n" +
                        "\nSDK memory usage after collection  = %,15.0f [bytes]" +

                        "\nmemoryUsedButFreed                 = %,15d [bytes]" +
                        "\nmaxMemoryUsedDuringCollection      = %,15d [bytes]" +
                        "\nmemoryUsedBeforeCollectionStart    = %,15d [bytes]" +
                        "\nmemoryUsedAfterCollectionFinished  = %,15d [bytes]",

                countersCount, cardinality,
                benchmarkSetup.aggregationTemporality, benchmarkSetup.memoryMode,
                sdkMeterProviderSizeOnHeap,
                attributesListSizeOnHeap,
                memoryUsedButFreed,
                maxUsedMemory.get(),
                usedMemoryBefore, usedMemoryAfter)
        );
    }

    private static void waitForGC() throws InterruptedException {
        List<Long> collectionCountBefore = getGCCollectionCount();
        boolean oneGcCountIncreased = false;
        int attempts = 0;
        do {
            System.gc();
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } while ((oneGcCountIncreased = oneOfGCCountIncreased(collectionCountBefore)) && ++attempts < 3);
        if (!oneGcCountIncreased) {
            fail("Failed to get GC in " + attempts + " attempts");
        }
    }

    private static boolean oneOfGCCountIncreased(List<Long> collectionCountBefore) {
        List<Long> gcCollectionCountNow = getGCCollectionCount();
        for (int i = 0; i < gcCollectionCountNow.size(); i++) {
            Long now = gcCollectionCountNow.get(i);
            if (now > collectionCountBefore.get(i)) {
                return true;
            }
        }
        return false;
    }

    private static List<Long> getGCCollectionCount() {
        List<Long> collectionCountBefore = new ArrayList<>();
        ManagementFactory.getGarbageCollectorMXBeans().forEach(garbageCollectorMXBean ->
                collectionCountBefore.add(garbageCollectorMXBean.getCollectionCount()));
        return collectionCountBefore;
    }

    private ScheduledFuture<?> startMeasuringUsedMemoryInBackground() {
        Runnable measureMaxMemory = () -> {
            try {
                long used = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
                if (used > maxUsedMemory.get()) {
                    maxUsedMemory.set(used);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        };

        return executorService.scheduleWithFixedDelay(measureMaxMemory, 0, 2, TimeUnit.MILLISECONDS);
    }

    private static void runBenchmark(AsynchronousMetricStorageGCBenchmark benchmark,
                                     AsynchronousMetricStorageGCBenchmark.ThreadState threadState) {
        benchmark.recordAndCollect(threadState);
    }
}
