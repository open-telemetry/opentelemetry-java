/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DoubleExponentialHistogramDataAggregatorTest {

  @Mock ExemplarReservoir reservoir;

  private static final DoubleExponentialHistogramAggregator aggregator =
      new DoubleExponentialHistogramAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          MetricDescriptor.create("name", "description", "unit"),
          /* stateful= */ false,
          ExemplarReservoir::noSamples);

  private static long valueToIndex(int scale, double value) {
    double scaleFactor = Math.scalb(1D / Math.log(2), scale);
    return (int) Math.floor(Math.log(value) * scaleFactor);
  }

  private static ExponentialHistogramAccumulation getTestAccumulation(
      List<ExemplarData> exemplars, double... recordings) {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    for (double r : recordings) {
      aggregatorHandle.recordDouble(r);
    }
    return aggregatorHandle.doAccumulateThenReset(exemplars);
  }

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle())
        .isInstanceOf(DoubleExponentialHistogramAggregator.Handle.class);
  }

  @Test
  void testRecordings() {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(0.5);
    aggregatorHandle.recordDouble(1.0);
    aggregatorHandle.recordDouble(12.0);
    aggregatorHandle.recordDouble(15.213);
    aggregatorHandle.recordDouble(12.0);
    aggregatorHandle.recordDouble(-13.2);
    aggregatorHandle.recordDouble(-2.01);
    aggregatorHandle.recordDouble(-1);
    aggregatorHandle.recordDouble(0.0);
    aggregatorHandle.recordLong(0);

    ExponentialHistogramAccumulation acc = aggregatorHandle.accumulateThenReset(Attributes.empty());
    List<Long> positiveCounts = Objects.requireNonNull(acc).getPositiveBuckets().getBucketCounts();
    List<Long> negativeCounts = acc.getNegativeBuckets().getBucketCounts();
    int expectedScale = 6; // should be downscaled from 20 to 6 after recordings

    assertThat(acc.getScale()).isEqualTo(expectedScale);
    assertThat(acc.getZeroCount()).isEqualTo(2);

    // Assert positive recordings are at correct index
    int posOffset = acc.getPositiveBuckets().getOffset();
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(5);
    assertThat(positiveCounts.get((int) valueToIndex(expectedScale, 0.5) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get((int) valueToIndex(expectedScale, 1.0) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get((int) valueToIndex(expectedScale, 12.0) - posOffset))
        .isEqualTo(2);
    assertThat(positiveCounts.get((int) valueToIndex(expectedScale, 15.213) - posOffset))
        .isEqualTo(1);

    // Assert negative recordings are at correct index
    int negOffset = acc.getNegativeBuckets().getOffset();
    assertThat(acc.getNegativeBuckets().getTotalCount()).isEqualTo(3);
    assertThat(negativeCounts.get((int) valueToIndex(expectedScale, 13.2) - negOffset))
        .isEqualTo(1);
    assertThat(negativeCounts.get((int) valueToIndex(expectedScale, 2.01) - negOffset))
        .isEqualTo(1);
    assertThat(negativeCounts.get((int) valueToIndex(expectedScale, 1.0) - negOffset)).isEqualTo(1);
  }

  @Test
  void testExemplarsInAccumulation() {
    DoubleExponentialHistogramAggregator agg =
        new DoubleExponentialHistogramAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            MetricDescriptor.create("name", "description", "unit"),
            /* stateful= */ false,
            () -> reservoir);

    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);

    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = agg.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());

    assertThat(
            Objects.requireNonNull(aggregatorHandle.accumulateThenReset(Attributes.empty()))
                .getExemplars())
        .isEqualTo(exemplars);
  }

  @Test
  void testAccumulationAndReset() {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordDouble(5.0);
    assertThat(
            Objects.requireNonNull(aggregatorHandle.accumulateThenReset(Attributes.empty()))
                .getPositiveBuckets()
                .getBucketCounts())
        .isEqualTo(Collections.singletonList(1L));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void testAccumulateData() {
    ExponentialHistogramAccumulation acc = aggregator.accumulateDouble(1.2);
    ExponentialHistogramAccumulation expected = getTestAccumulation(Collections.emptyList(), 1.2);
    assertThat(acc).isEqualTo(expected);
  }

  @Test
  void testMergeAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    List<ExemplarData> previousExemplars =
        Collections.singletonList(
            DoubleExemplarData.create(attributes, 1L, "spanId", "traceId", 2));
    ExponentialHistogramAccumulation previousAccumulation =
        getTestAccumulation(previousExemplars, 0, 4.1, 100, 100, 10000, 1000000);
    ExponentialHistogramAccumulation nextAccumulation =
        getTestAccumulation(exemplars, -1000, -2000000, -8.2, 2.3);

    // Merged accumulations should equal accumulation with equivalent recordings and latest
    // exemplars.
    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
        .isEqualTo(
            getTestAccumulation(
                exemplars, 0, 4.1, 100, 100, 10000, 1000000, -1000, -2000000, -8.2, 2.3));
  }

  @Test
  void testMergeNonOverlap() {
    ExponentialHistogramAccumulation previousAccumulation =
        getTestAccumulation(Collections.emptyList(), 10, 100, 100, 10000, 100000);
    ExponentialHistogramAccumulation nextAccumulation =
        getTestAccumulation(Collections.emptyList(), 0.001, 0.01, 0.1, 1);

    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
        .isEqualTo(
            getTestAccumulation(
                Collections.emptyList(), 0.001, 0.01, 0.1, 1, 10, 100, 100, 10000, 100000));
  }

  @Test
  void testMergeOverlap() {
    ExponentialHistogramAccumulation previousAccumulation =
        getTestAccumulation(Collections.emptyList(), 0, 10, 100, 10000, 100000);
    ExponentialHistogramAccumulation nextAccumulation =
        getTestAccumulation(Collections.emptyList(), 100000, 10000, 100, 10, 0);

    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
        .isEqualTo(
            getTestAccumulation(
                Collections.emptyList(), 0, 0, 10, 10, 100, 100, 10000, 10000, 100000, 100000));
  }

  @Test
  void testInsert1M() {
    AggregatorHandle<ExponentialHistogramAccumulation> handle = aggregator.createHandle();

    final double min = 1.0 / (1 << 16);
    final int n = 1024 * 1024 - 1;
    double d = min;
    for (int i = 0; i < n; i++) {
      handle.recordDouble(d);
      d += min;
    }

    ExponentialHistogramAccumulation acc = handle.accumulateThenReset(Attributes.empty());
    assertThat(Objects.requireNonNull(acc).getScale()).isEqualTo(4);
    assertThat(acc.getPositiveBuckets().getBucketCounts().size()).isEqualTo(320);
  }

  @Test
  void testDownScale() {
    DoubleExponentialHistogramAggregator.Handle handle =
        (DoubleExponentialHistogramAggregator.Handle) aggregator.createHandle();
    handle.downScale(20); // down to zero scale

    // test histogram operates properly after being manually scaled down to 0
    handle.recordDouble(0.5);
    handle.recordDouble(1.0);
    handle.recordDouble(2.0);
    handle.recordDouble(4.0);
    handle.recordDouble(16.0);

    ExponentialHistogramAccumulation acc = handle.accumulateThenReset(Attributes.empty());
    assertThat(Objects.requireNonNull(acc).getScale()).isEqualTo(0);
    ExponentialHistogramBuckets buckets = acc.getPositiveBuckets();
    assertThat(acc.getSum()).isEqualTo(23.5);
    assertThat(buckets.getOffset()).isEqualTo(-1);
    assertThat(buckets.getBucketCounts()).isEqualTo(Arrays.asList(1L, 1L, 1L, 1L, 0L, 1L));
  }

  @Test
  void testToMetricData() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    @SuppressWarnings("unchecked")
    Supplier<ExemplarReservoir> reservoirSupplier = Mockito.mock(Supplier.class);
    Mockito.when(reservoir.collectAndReset(Attributes.empty()))
        .thenReturn(Collections.singletonList(exemplar));
    Mockito.when(reservoirSupplier.get()).thenReturn(reservoir);

    DoubleExponentialHistogramAggregator cumulativeAggregator =
        new DoubleExponentialHistogramAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            MetricDescriptor.create("name", "description", "unit"),
            /* stateful= */ true,
            reservoirSupplier);

    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle =
        cumulativeAggregator.createHandle();
    aggregatorHandle.recordDouble(0);
    aggregatorHandle.recordDouble(0);
    aggregatorHandle.recordDouble(123.456);
    ExponentialHistogramAccumulation acc = aggregatorHandle.accumulateThenReset(Attributes.empty());

    MetricData metricData =
        cumulativeAggregator.toMetricData(
            Collections.singletonMap(Attributes.empty(), acc), 0, 10, 100);

    // Assertions run twice to verify immutability; recordings shouldn't modify the metric data
    for (int i = 0; i < 2; i++) {
      assertThat(metricData)
          .hasExponentialHistogram()
          .isCumulative()
          .points()
          .satisfiesExactly(
              point -> {
                assertThat(point)
                    .hasSum(123.456)
                    .hasScale(20)
                    .hasZeroCount(2)
                    .hasTotalCount(3)
                    .hasExemplars(exemplar);
                assertThat(point.getPositiveBuckets())
                    .hasCounts(Collections.singletonList(1L))
                    .hasOffset((int) valueToIndex(20, 123.456))
                    .hasTotalCount(1);
                assertThat(point.getNegativeBuckets())
                    .hasTotalCount(0)
                    .hasCounts(Collections.emptyList());
              });
      aggregatorHandle.recordDouble(1);
      aggregatorHandle.recordDouble(-1);
      aggregatorHandle.recordDouble(0);
    }
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    final ExponentialHistogram summarizer = new ExponentialHistogram();
    final ImmutableList<Double> updates = ImmutableList.of(0D, 0.1D, -0.1D, 1D, -1D, 100D);
    final int numberOfThreads = updates.size();
    final int numberOfUpdates = 10000;
    final ThreadPoolExecutor executor =
        (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

    executor.invokeAll(
        updates.stream()
            .map(
                v ->
                    Executors.callable(
                        () -> {
                          for (int j = 0; j < numberOfUpdates; j++) {
                            aggregatorHandle.recordDouble(v);
                            if (ThreadLocalRandom.current().nextInt(10) == 0) {
                              summarizer.process(
                                  aggregatorHandle.accumulateThenReset(Attributes.empty()));
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    // make sure everything gets merged when all the aggregation is done.
    summarizer.process(aggregatorHandle.accumulateThenReset(Attributes.empty()));

    ExponentialHistogramAccumulation acc = Objects.requireNonNull(summarizer.accumulation);
    assertThat(acc.getZeroCount()).isEqualTo(numberOfUpdates);
    assertThat(acc.getSum()).isCloseTo(100.0D * 10000, Offset.offset(0.0001)); // float error
    assertThat(acc.getScale()).isEqualTo(5);
    assertThat(acc.getPositiveBuckets()).hasTotalCount(numberOfUpdates * 3).hasOffset(-107);
    assertThat(acc.getNegativeBuckets()).hasTotalCount(numberOfUpdates * 2).hasOffset(-107);

    // Verify positive buckets have correct counts
    List<Long> posCounts = acc.getPositiveBuckets().getBucketCounts();
    assertThat(
            posCounts.get(
                (int) valueToIndex(acc.getScale(), 0.1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(
                (int) valueToIndex(acc.getScale(), 1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(
                (int) valueToIndex(acc.getScale(), 100) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);

    // Verify negative buckets have correct counts
    List<Long> negCounts = acc.getNegativeBuckets().getBucketCounts();
    assertThat(
            negCounts.get(
                (int) valueToIndex(acc.getScale(), 0.1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            negCounts.get(
                (int) valueToIndex(acc.getScale(), 1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
  }

  private static final class ExponentialHistogram {
    private final Object mutex = new Object();

    @Nullable private ExponentialHistogramAccumulation accumulation;

    void process(@Nullable ExponentialHistogramAccumulation other) {
      if (other == null) {
        return;
      }

      synchronized (mutex) {
        if (accumulation == null) {
          accumulation = other;
          return;
        }
        accumulation = aggregator.merge(accumulation, other);
      }
    }
  }
}
