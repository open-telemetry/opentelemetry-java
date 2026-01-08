/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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

  private static final int cardinality = 100;
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
      SdkMeterProvider sdkMeterProvider =
          SdkMeterProvider.builder()
              .registerMetricReader(
                  PeriodicMetricReader.builder(
                          // Configure an exporter that configures the temporality and aggregation
                          // for the test case, but otherwise drops the data on export
                          new NoopMetricExporter(
                              aggregationTemporality, aggregationGenerator.aggregation))
                      // Effectively disable periodic reading so reading is only done on #flush()
                      .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
                      .build())
              // Disable exemplars
              .setExemplarFilter(ExemplarFilter.alwaysOff())
              .build();

      histogram = sdkMeterProvider.get("meter").histogramBuilder("histogram").build();

      random = new Random();
      attributesList = new ArrayList<>(cardinality);
      String last = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
      for (int i = 0; i < cardinality; i++) {
        char[] chars = last.toCharArray();
        chars[random.nextInt(last.length())] = (char) (random.nextInt(26) + 'a');
        last = new String(chars);
        attributesList.add(Attributes.builder().put("key", last).build());
      }
    }

    @TearDown
    public void tearDown() {
      sdkMeterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
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

  private static class NoopMetricExporter implements MetricExporter {
    private final AggregationTemporality aggregationTemporality;
    private final Aggregation aggregation;

    private NoopMetricExporter(
        AggregationTemporality aggregationTemporality, Aggregation aggregation) {
      this.aggregationTemporality = aggregationTemporality;
      this.aggregation = aggregation;
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
      return aggregation;
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
      return aggregationTemporality;
    }
  }
}
