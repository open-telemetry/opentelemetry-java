/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jol.info.GraphLayout;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 20, batchSize = 100)
@Warmup(iterations = 10, batchSize = 10)
@Fork(1)
public class AsynchronousCounterBenchmark {
  private static final Logger logger = Logger.getLogger(AsynchronousCounterBenchmark.class.getName());

  private static final int cardinality = 100_000; // 1000
  private static final int countersCount = 50; // 10

  @State(value = Scope.Benchmark)
  @SuppressWarnings("SystemOut")
  public static class ThreadState {
    @Param private AggregationTemporality aggregationTemporality;
    @Param private MemoryMode memoryMode;
    private SdkMeterProvider sdkMeterProvider;
    private Random random;
    private List<Attributes> attributesList;
//    private Map<String, String> pooledHashMap;
//    private List<String> keys;

    public ThreadState() {}

    @SuppressWarnings("SpellCheckingInspection")
    @Setup
    public void setup() {
      PeriodicMetricReader metricReader = PeriodicMetricReader.builder(
              // Configure an exporter that configures the temporality and aggregation
              // for the test case, but otherwise drops the data on export
              new NoopMetricExporter(aggregationTemporality, Aggregation.sum(), memoryMode))
          // Effectively disable periodic reading so reading is only done on #flush()
          .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
          .build();
      SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
      SdkMeterProviderUtil.registerMetricReaderWithCardinalitySelector(
          builder,
          metricReader,
          unused -> cardinality+10
      );

      random = new Random();
      HashSet<String> attributeSet = new HashSet<>();
      attributesList = new ArrayList<>(cardinality);
      String last = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
      for (int i = 0; i < cardinality; i++) {
        char[] chars = last.toCharArray();
        int attempts = 0;
        do {
          chars[random.nextInt(last.length())] = (char) (random.nextInt(26) + 'a');
        } while (attributeSet.contains(new String(chars)) && ++attempts < 1000);
        if (attributeSet.contains(new String(chars))) {
            throw new IllegalStateException("bad attribute");
        }
        last = new String(chars);
        attributesList.add(Attributes.builder().put("key", last).build());
        boolean exists = !attributeSet.add(last);
        if (exists) {
            throw new IllegalStateException("exists");
        }
      }
      if (attributeSet.size() != attributesList.size()) {
        throw new IllegalStateException("Not same size "+attributeSet.size()+", "+attributesList.size());
      }
      logger.info("Size: "+attributesList.size());
      Map<Attributes, Integer> map = new HashMap<>();
      attributesList.forEach(v -> map.putIfAbsent(v, 1));
      if (map.size() != attributesList.size()) {
        throw new IllegalStateException("not same size as map");
      }
      // Disable examplars
      SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOff());
      sdkMeterProvider = builder.build();
      for (int i = 0; i < countersCount; i++) {
        sdkMeterProvider.get("meter").counterBuilder("counter" + i)
            .buildWithCallback(observableLongMeasurement -> {
                for (int j = 0; j < attributesList.size(); j++) {
                    Attributes attributes = attributesList.get(j);
                    observableLongMeasurement.record(random.nextInt(10_000), attributes);
                }
            });
      }
    }

    @TearDown
    public void tearDown() {
      sdkMeterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
  }

  @Benchmark
  @Threads(value = 1)
  @SuppressWarnings("SystemOut")
  public void recordAndCollect(ThreadState threadState) {
    threadState.sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
  }

  public void measureMemoryUsage() {
    StringBuilder attributesSummary = new StringBuilder().append("\n");
    StringBuilder resultSummaryWithAttributes = new StringBuilder().append("\n");

    AggregationTemporality aggregationTemporality = AggregationTemporality.CUMULATIVE;
    //logger.log(Level.INFO, "Starting {0}...", new Object[]{aggregationTemporality});
    ThreadState threadState = new ThreadState();
    threadState.aggregationTemporality = aggregationTemporality;
    threadState.memoryMode = MemoryMode.IMMUTABLE_DATA;

    long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    logger.log(Level.INFO, "Memory before: " + memory);
    threadState.setup();

//      attributesSummary.append(String.format("%10s: %,15.0f [bytes]",
//              threadState.aggregationTemporality,
//              (double) GraphLayout.parseInstance(threadState.sdkMeterProvider).totalSize()))
//          .append("\n");
    //warmup(threadState, 100);

    measure(2, threadState);
    memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    logger.log(Level.INFO, "Memory after: " + memory);
    System.gc();
    memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    logger.log(Level.INFO, "Memory after GC: " + memory);

    //logger.log(Level.INFO, "Done");
      GraphLayout graphLayout = GraphLayout.parseInstance(threadState.sdkMeterProvider);
      resultSummaryWithAttributes.append(String.format("%10s: %,15.0f [bytes]",
              threadState.aggregationTemporality,
              (double) graphLayout.totalSize()))
          .append("\n");


      //      resultSummaryWithAttributes.append("\n").append(graphLayout.toFootprint());

    graphLayout = GraphLayout.parseInstance(threadState.attributesList);
    attributesSummary.append(String.format("%,15.0f [bytes]",
            (double) graphLayout.totalSize()))
        .append("\n");

    threadState.tearDown();

    logger.info("\nAttributes:\n" + attributesSummary
        +"\nWithAttributes:\n"
        + resultSummaryWithAttributes);
  }

  private void measure(int x, ThreadState threadState) {
    for (int i = 0; i < x; i++) {
      //logger.log(Level.INFO, "Recording values...["+i+"]");
      recordAndCollect(threadState);
    }
  }

//  private void warmup(ThreadState threadState, int numberOfTimes) {
//    for (int i = 0; i < numberOfTimes; i++) {
//      //logger.log(Level.INFO, "Recording values...["+i+"]");
//      recordAndCollect(threadState);
//    }
//
//  }

}
