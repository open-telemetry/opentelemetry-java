/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.prototype.AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype.Filter;
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

@SuppressWarnings("SystemOut")
public class AsynchronousCounterMemoryUsageBenchmark {

  ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  AtomicLong maxUsedMemory = new AtomicLong(0);

  private AsynchronousCounterMemoryUsageBenchmark() {}

  /**
   * Runs the memory usage benchmark.
   *
   * <p>Set the parameters in {@link AsynchronousCounterMemoryUsageBenchmark#measure()}
   *
   * @param args Unused
   */
  public static void main(String[] args)
      throws ExecutionException, InterruptedException, TimeoutException {
    AsynchronousCounterMemoryUsageBenchmark asynchronousCounterMemoryUsageBenchmark =
        new AsynchronousCounterMemoryUsageBenchmark();
    asynchronousCounterMemoryUsageBenchmark.measure();
  }

  @SuppressWarnings("DefaultCharset")
  private void measure()
      throws ExecutionException, InterruptedException, TimeoutException {
    // Parameters
    AggregationTemporality aggregationTemporality = AggregationTemporality.CUMULATIVE;
    int countersCount = 50;
    int cardinality = 100_000;
    int countersToAllows = 5;
    int attributesToAllow = 1000;
    Filter filter = Filter.WITH_FILTER;

    AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype.ThreadState benchmarkSetup =
        new AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype.ThreadState(
            countersCount, cardinality, attributesToAllow, countersToAllows);

    benchmarkSetup.aggregationTemporality = aggregationTemporality;
    benchmarkSetup.filter = filter;

    AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype benchmark =
        new AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype();

    benchmarkSetup.setup();

    waitForGarbageCollection();
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
    }

    waitForGarbageCollection();
    long usedMemoryAfter = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    long memoryUsedButFreed = maxUsedMemory.get() - usedMemoryAfter;

    System.out.printf(
        ""
            + "\nCounters = %d, Cardinality = %,d"
            + "\n%s: "
            + "\nmemoryUsedButFreed                 = %,15d [bytes]"
            + "\nmaxMemoryUsedDuringCollection      = %,15d [bytes]"
            + "\nmemoryUsedBeforeCollectionStart    = %,15d [bytes]"
            + "\nmemoryUsedAfterCollectionFinished  = %,15d [bytes]%n",
        countersCount,
        cardinality,
        benchmarkSetup.aggregationTemporality,
        memoryUsedButFreed,
        maxUsedMemory.get(),
        usedMemoryBefore,
        usedMemoryAfter);
  }

  private static void waitForGarbageCollection() throws InterruptedException {
    List<Long> collectionCountBefore = getGarbageCollectorsCollectionCount();
    boolean oneGcCountIncreased;
    int attempts = 0;
    do {
      System.gc();
      Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    } while ((oneGcCountIncreased = oneOfGarbageCollectorsCountIncreased(collectionCountBefore))
        && ++attempts < 3);
    if (!oneGcCountIncreased) {
      throw new RuntimeException("Failed to get GC in " + attempts + " attempts");
    }
  }

  private static boolean oneOfGarbageCollectorsCountIncreased(List<Long> collectionCountBefore) {
    List<Long> gcCollectionCountNow = getGarbageCollectorsCollectionCount();
    for (int i = 0; i < gcCollectionCountNow.size(); i++) {
      Long now = gcCollectionCountNow.get(i);
      if (now > collectionCountBefore.get(i)) {
        return true;
      }
    }
    return false;
  }

  private static List<Long> getGarbageCollectorsCollectionCount() {
    List<Long> collectionCountBefore = new ArrayList<>();
    ManagementFactory.getGarbageCollectorMXBeans()
        .forEach(
            garbageCollectorMXBean ->
                collectionCountBefore.add(garbageCollectorMXBean.getCollectionCount()));
    return collectionCountBefore;
  }

  private ScheduledFuture<?> startMeasuringUsedMemoryInBackground() {
    Runnable measureMaxMemory =
        () -> {
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

  private static void runBenchmark(
      AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype benchmark,
      AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype.ThreadState threadState) {
    benchmark.recordAndCollect(threadState);
  }
}
