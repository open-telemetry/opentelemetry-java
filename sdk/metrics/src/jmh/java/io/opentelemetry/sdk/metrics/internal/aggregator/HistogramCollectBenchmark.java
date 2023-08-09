/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Measures the collection path for various histogram aggregations. Should be used primarily to
 * compare memory allocation rates.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 5, batchSize = 100)
@Warmup(iterations = 2, batchSize = 10)
@Fork(1)
public class HistogramCollectBenchmark {
  private static final Logger logger = Logger.getLogger(HistogramCollectBenchmark.class.getName());

  private static final int cardinality = 100_000;
  private static final int measurementsPerSeries = 10_000;

  @State(Scope.Benchmark)
  public static class ThreadState {
    @Param private AggregationTemporality aggregationTemporality;
    @Param private AggregationGenerator aggregationGenerator;

    private SdkMeterProvider sdkMeterProvider;
    private DoubleHistogram histogram;
    private Random random;
    private List<Attributes> attributesList;

    @Setup
    public void setup() {
      PeriodicMetricReader metricReader = PeriodicMetricReader.builder(
              // Configure an exporter that configures the temporality and aggregation
              // for the test case, but otherwise drops the data on export
              new NoopMetricExporter(
                  aggregationTemporality, aggregationGenerator.aggregation))
          // Effectively disable periodic reading so reading is only done on #flush()
          .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
          .build();
      SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
      SdkMeterProviderUtil.registerMetricReaderWithCardinalitySelector(
          builder,
          metricReader,
          unused -> cardinality
          );
      // Disable examplars
      SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOff());
      sdkMeterProvider = builder.build();
      histogram = sdkMeterProvider.get("meter").histogramBuilder("histogram").build();

      random = new Random();
      attributesList = AttributesGenerator.generate(cardinality);
    }

    @TearDown
    public void tearDown() {
      sdkMeterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
  }

  public void measureMemoryUsage() {
    StringBuilder resultSummaryWithoutAttributes = new StringBuilder().append("\n");
    StringBuilder resultSummaryWithAttributes = new StringBuilder().append("\n");

    AggregationGenerator[] aggregationGenerators = new AggregationGenerator[]{AggregationGenerator.EXPLICIT_BUCKET_HISTOGRAM}; //AggregationGenerator.values();
    for (AggregationGenerator aggregationGenerator : aggregationGenerators) {
      AggregationTemporality[] aggregationTemporalities = new AggregationTemporality[]{AggregationTemporality.CUMULATIVE}; //AggregationTemporality.values();
      for (AggregationTemporality aggregationTemporality : aggregationTemporalities) {
        logger.log(Level.INFO, "Starting {0}, {1}...", new Object[]{aggregationGenerator, aggregationTemporality});
        ThreadState threadState = new ThreadState();
        threadState.aggregationGenerator = aggregationGenerator;
        threadState.aggregationTemporality = aggregationTemporality;
        threadState.setup();

        resultSummaryWithoutAttributes.append(String.format("%50s, %10s: %,15.0f [bytes]",
                threadState.aggregationGenerator,
                threadState.aggregationTemporality,
                (double) GraphLayout.parseInstance(threadState.sdkMeterProvider).totalSize()))
            .append("\n");

        logger.log(Level.INFO, "Recording values...", new Object[]{aggregationGenerator, aggregationTemporality});
        recordAndCollect(threadState);

        logger.log(Level.INFO, "Done", new Object[]{aggregationGenerator, aggregationTemporality});
        GraphLayout graphLayout = GraphLayout.parseInstance(threadState.sdkMeterProvider);
        resultSummaryWithAttributes.append(String.format("%50s, %10s: %,15.0f [bytes]",
                threadState.aggregationGenerator,
                threadState.aggregationTemporality,
                (double) graphLayout.totalSize()))
            .append("\n");

        resultSummaryWithAttributes.append("\n").append(graphLayout.toFootprint());
      }
    }
    logger.info(resultSummaryWithoutAttributes
        +"\n"
        + resultSummaryWithAttributes);
  }

  @Benchmark
  @Threads(value = 1)
  public void recordAndCollect(ThreadState threadState) {
    for (Attributes attributes : threadState.attributesList) {
      for (int i = 0; i < measurementsPerSeries; i++) {
        int value = threadState.random.nextInt(10_000);
        threadState.histogram.record(value, attributes);
      }
    }
    threadState.sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
  }

  @SuppressWarnings("ImmutableEnumChecker")
  public enum AggregationGenerator {
    EXPLICIT_BUCKET_HISTOGRAM(Aggregation.explicitBucketHistogram()),
    DEFAULT_BASE2_EXPONENTIAL_BUCKET_HISTOGRAM(Aggregation.base2ExponentialBucketHistogram()),
    ZERO_MAX_SCALE_BASE2_EXPONENTIAL_BUCKET_HISTOGRAM(
        Aggregation.base2ExponentialBucketHistogram(160, 0));

    private final Aggregation aggregation;

    AggregationGenerator(Aggregation aggregation) {
      this.aggregation = aggregation;
    }
  }

}
