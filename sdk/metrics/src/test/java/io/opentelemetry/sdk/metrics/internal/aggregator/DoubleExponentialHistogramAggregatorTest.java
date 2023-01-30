/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
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
import java.util.stream.Stream;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoubleExponentialHistogramAggregatorTest {

  @Mock ExemplarReservoir<DoubleExemplarData> reservoir;

  private static final int MAX_SCALE = 20;
  private static final DoubleExponentialHistogramAggregator aggregator =
      new DoubleExponentialHistogramAggregator(ExemplarReservoir::doubleNoSamples, 160, 20);
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");

  private static Stream<DoubleExponentialHistogramAggregator> provideAggregator() {
    return Stream.of(
        aggregator,
        new DoubleExponentialHistogramAggregator(
            ExemplarReservoir::doubleNoSamples, 160, MAX_SCALE));
  }

  private static int valueToIndex(int scale, double value) {
    double scaleFactor = Math.scalb(1D / Math.log(2), scale);
    return (int) Math.ceil(Math.log(value) * scaleFactor) - 1;
  }

  private static ExponentialHistogramAccumulation getTestAccumulation(
      List<DoubleExemplarData> exemplars, double... recordings) {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    for (double r : recordings) {
      aggregatorHandle.recordDouble(r);
    }
    return aggregatorHandle.doAccumulateThenMaybeReset(exemplars, /* reset= */ true);
  }

  @Test
  void createHandle() {
    AggregatorHandle<?, ?> handle = aggregator.createHandle();
    assertThat(handle).isInstanceOf(DoubleExponentialHistogramAggregator.Handle.class);
    ExponentialHistogramAccumulation accumulation =
        ((DoubleExponentialHistogramAggregator.Handle) handle)
            .doAccumulateThenMaybeReset(Collections.emptyList(), /* reset= */ true);
    assertThat(accumulation.getPositiveBuckets())
        .isInstanceOf(DoubleExponentialHistogramAggregator.EmptyExponentialHistogramBuckets.class);
    assertThat(accumulation.getPositiveBuckets().getScale()).isEqualTo(MAX_SCALE);
    assertThat(accumulation.getNegativeBuckets())
        .isInstanceOf(DoubleExponentialHistogramAggregator.EmptyExponentialHistogramBuckets.class);
    assertThat(accumulation.getNegativeBuckets().getScale()).isEqualTo(MAX_SCALE);
  }

  @Test
  void testRecordings() {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
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

    ExponentialHistogramAccumulation acc =
        aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true);
    List<Long> positiveCounts = Objects.requireNonNull(acc).getPositiveBuckets().getBucketCounts();
    List<Long> negativeCounts = acc.getNegativeBuckets().getBucketCounts();
    int expectedScale = 5; // should be downscaled from 20 to 5 after recordings

    assertThat(acc.getScale()).isEqualTo(expectedScale);
    assertThat(acc.getPositiveBuckets().getScale()).isEqualTo(expectedScale);
    assertThat(acc.getNegativeBuckets().getScale()).isEqualTo(expectedScale);
    assertThat(acc.getZeroCount()).isEqualTo(2);

    // Assert positive recordings are at correct index
    int posOffset = acc.getPositiveBuckets().getOffset();
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(5);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 0.5) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 1.0) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 12.0) - posOffset)).isEqualTo(2);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 15.213) - posOffset)).isEqualTo(1);

    // Assert negative recordings are at correct index
    int negOffset = acc.getNegativeBuckets().getOffset();
    assertThat(acc.getNegativeBuckets().getTotalCount()).isEqualTo(3);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 13.2) - negOffset)).isEqualTo(1);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 2.01) - negOffset)).isEqualTo(1);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 1.0) - negOffset)).isEqualTo(1);
  }

  @Test
  void testInvalidRecording() {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    // Non finite recordings should be ignored
    aggregatorHandle.recordDouble(Double.POSITIVE_INFINITY);
    aggregatorHandle.recordDouble(Double.NEGATIVE_INFINITY);
    aggregatorHandle.recordDouble(Double.NaN);

    ExponentialHistogramAccumulation acc =
        aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true);
    assertThat(Objects.requireNonNull(acc).getSum()).isEqualTo(0);
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(0);
    assertThat(acc.getNegativeBuckets().getTotalCount()).isEqualTo(0);
    assertThat(acc.getZeroCount()).isEqualTo(0);
  }

  @ParameterizedTest
  @MethodSource("provideAggregator")
  void testRecordingsAtLimits(DoubleExponentialHistogramAggregator aggregator) {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();

    aggregatorHandle.recordDouble(Double.MIN_VALUE);
    aggregatorHandle.recordDouble(Double.MAX_VALUE);

    ExponentialHistogramAccumulation acc =
        aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true);
    List<Long> bucketCounts = Objects.requireNonNull(acc).getPositiveBuckets().getBucketCounts();

    // assert buckets == [1 0 0 0 ... 1]
    assertThat(bucketCounts.get(0)).isEqualTo(1);
    assertThat(bucketCounts.get(bucketCounts.size() - 1)).isEqualTo(1);
    assertThat(bucketCounts.stream().filter(i -> i == 0).count())
        .isEqualTo(bucketCounts.size() - 2);
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(2);

    // With 160 buckets allowed, minimum scale is -4
    assertThat(acc.getScale()).isEqualTo(-4);
    assertThat(acc.getPositiveBuckets().getScale()).isEqualTo(-4);
    assertThat(acc.getNegativeBuckets().getScale()).isEqualTo(-4);

    // if scale is -4, base is 65,536.
    int base = 65_536;

    // Verify the rule holds:
    // base ^ (offset+i) <= (values recorded to bucket i) < base ^ (offset+i+1)

    // lowest bucket
    // As the bucket lower bound is less than Double.MIN_VALUE, Math.pow() rounds to 0
    assertThat(Math.pow(base, acc.getPositiveBuckets().getOffset())).isEqualTo(0);
    assertThat(Math.pow(base, acc.getPositiveBuckets().getOffset() + 1))
        .isGreaterThan(Double.MIN_VALUE);

    // highest bucket
    assertThat(Math.pow(base, acc.getPositiveBuckets().getOffset() + bucketCounts.size() - 1))
        .isLessThanOrEqualTo(Double.MAX_VALUE);
    // As the bucket upper bound is greater than Double.MAX_VALUE, Math.pow() rounds to infinity
    assertThat(Math.pow(base, acc.getPositiveBuckets().getOffset() + bucketCounts.size()))
        .isEqualTo(Double.POSITIVE_INFINITY);
  }

  @Test
  void testExemplarsInAccumulation() {
    DoubleExponentialHistogramAggregator agg =
        new DoubleExponentialHistogramAggregator(() -> reservoir, 160, MAX_SCALE);

    Attributes attributes = Attributes.builder().put("test", "value").build();
    DoubleExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<DoubleExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);

    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        agg.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());

    assertThat(
            Objects.requireNonNull(
                    aggregatorHandle.accumulateThenMaybeReset(
                        Attributes.empty(), /* reset= */ true))
                .getExemplars())
        .isEqualTo(exemplars);
  }

  @Test
  void testAccumulationAndReset() {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true))
        .isNull();

    aggregatorHandle.recordDouble(5.0);
    assertThat(
            Objects.requireNonNull(
                    aggregatorHandle.accumulateThenMaybeReset(
                        Attributes.empty(), /* reset= */ true))
                .getPositiveBuckets()
                .getBucketCounts())
        .isEqualTo(Collections.singletonList(1L));
    assertThat(aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true))
        .isNull();
  }

  @Test
  void testAccumulateData() {
    ExponentialHistogramAccumulation acc =
        aggregator.accumulateDoubleMeasurement(1.2, Attributes.empty(), Context.current());
    ExponentialHistogramAccumulation expected = getTestAccumulation(Collections.emptyList(), 1.2);
    assertThat(acc).isEqualTo(expected);
  }

  @Test
  void testInsert1M() {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> handle =
        aggregator.createHandle();

    int n = 1024 * 1024 - 1;
    double min = 16.0 / n;
    double d = min;
    for (int i = 0; i < n; i++) {
      handle.recordDouble(d);
      d += min;
    }

    ExponentialHistogramAccumulation acc =
        Objects.requireNonNull(
            handle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true));
    assertThat(acc.getScale()).isEqualTo(3);
    assertThat(acc.getPositiveBuckets().getScale()).isEqualTo(3);
    assertThat(acc.getNegativeBuckets().getScale()).isEqualTo(3);
    assertThat(acc.getPositiveBuckets().getBucketCounts().size()).isEqualTo(160);
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(n);
  }

  @Test
  void testDownScale() {
    DoubleExponentialHistogramAggregator.Handle handle =
        (DoubleExponentialHistogramAggregator.Handle) aggregator.createHandle();
    // record a measurement to initialize positive buckets
    handle.recordDouble(0.5);

    handle.downScale(20); // down to zero scale

    // test histogram operates properly after being manually scaled down to 0
    handle.recordDouble(1.0);
    handle.recordDouble(2.0);
    handle.recordDouble(4.0);
    handle.recordDouble(16.0);

    ExponentialHistogramAccumulation acc =
        Objects.requireNonNull(
            handle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true));
    assertThat(acc.getScale()).isEqualTo(0);
    assertThat(acc.getPositiveBuckets().getScale()).isEqualTo(0);
    assertThat(acc.getNegativeBuckets().getScale()).isEqualTo(0);
    ExponentialHistogramBuckets buckets = acc.getPositiveBuckets();
    assertThat(acc.getSum()).isEqualTo(23.5);
    assertThat(buckets.getOffset()).isEqualTo(-2);
    assertThat(buckets.getBucketCounts()).isEqualTo(Arrays.asList(1L, 1L, 1L, 1L, 0L, 1L));
    assertThat(buckets.getTotalCount()).isEqualTo(5);
  }

  @Test
  void testToMetricData() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    DoubleExemplarData exemplar =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    @SuppressWarnings("unchecked")
    Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier =
        Mockito.mock(Supplier.class);
    Mockito.when(reservoir.collectAndReset(Attributes.empty()))
        .thenReturn(Collections.singletonList(exemplar));
    Mockito.when(reservoirSupplier.get()).thenReturn(reservoir);

    DoubleExponentialHistogramAggregator cumulativeAggregator =
        new DoubleExponentialHistogramAggregator(reservoirSupplier, 160, MAX_SCALE);

    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        cumulativeAggregator.createHandle();
    aggregatorHandle.recordDouble(0);
    aggregatorHandle.recordDouble(0);
    aggregatorHandle.recordDouble(123.456);
    ExponentialHistogramAccumulation acc =
        aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ true);

    MetricData metricDataCumulative =
        cumulativeAggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonMap(Attributes.empty(), acc),
            AggregationTemporality.CUMULATIVE,
            0,
            10,
            100);

    // Assertions run twice to verify immutability; recordings shouldn't modify the metric data
    for (int i = 0; i < 2; i++) {
      assertThat(metricDataCumulative)
          .hasExponentialHistogramSatisfying(
              expHistogram ->
                  expHistogram
                      .isCumulative()
                      .hasPointsSatisfying(
                          point ->
                              point
                                  .hasSum(123.456)
                                  .hasScale(20)
                                  .hasZeroCount(2)
                                  .hasCount(3)
                                  .hasMin(0)
                                  .hasMax(123.456)
                                  .hasExemplars(exemplar)
                                  .hasPositiveBucketsSatisfying(
                                      buckets ->
                                          buckets
                                              .hasCounts(Collections.singletonList(1L))
                                              .hasOffset(valueToIndex(20, 123.456))
                                              .hasTotalCount(1))
                                  .hasNegativeBucketsSatisfying(
                                      buckets ->
                                          buckets
                                              .hasTotalCount(0)
                                              .hasCounts(Collections.emptyList()))));
      aggregatorHandle.recordDouble(1);
      aggregatorHandle.recordDouble(-1);
      aggregatorHandle.recordDouble(0);
    }

    MetricData metricDataDelta =
        cumulativeAggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonMap(Attributes.empty(), acc),
            AggregationTemporality.DELTA,
            0,
            10,
            100);
    assertThat(metricDataDelta.getType()).isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);
    assertThat(metricDataDelta.getExponentialHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    AggregatorHandle<ExponentialHistogramAccumulation, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    ImmutableList<Double> updates = ImmutableList.of(0D, 0.1D, -0.1D, 1D, -1D, 100D);
    int numberOfThreads = updates.size();
    int numberOfUpdates = 10000;
    ThreadPoolExecutor executor =
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
                              aggregatorHandle.accumulateThenMaybeReset(
                                  Attributes.empty(), /* reset= */ false);
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    ExponentialHistogramAccumulation acc =
        Objects.requireNonNull(
            aggregatorHandle.accumulateThenMaybeReset(Attributes.empty(), /* reset= */ false));
    assertThat(acc.getZeroCount()).isEqualTo(numberOfUpdates);
    assertThat(acc.getSum()).isCloseTo(100.0D * 10000, Offset.offset(0.0001)); // float error
    assertThat(acc.getScale()).isEqualTo(3);
    assertThat(acc.getPositiveBuckets().getScale()).isEqualTo(3);
    assertThat(acc.getNegativeBuckets().getScale()).isEqualTo(3);
    ExponentialHistogramBuckets positiveBuckets = acc.getPositiveBuckets();
    assertThat(positiveBuckets.getTotalCount()).isEqualTo(numberOfUpdates * 3);
    assertThat(positiveBuckets.getOffset()).isEqualTo(-27);
    ExponentialHistogramBuckets negativeBuckets = acc.getNegativeBuckets();
    assertThat(negativeBuckets.getTotalCount()).isEqualTo(numberOfUpdates * 2);
    assertThat(negativeBuckets.getOffset()).isEqualTo(-27);

    // Verify positive buckets have correct counts
    List<Long> posCounts = acc.getPositiveBuckets().getBucketCounts();
    assertThat(
            posCounts.get(valueToIndex(acc.getScale(), 0.1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(valueToIndex(acc.getScale(), 1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(valueToIndex(acc.getScale(), 100) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);

    // Verify negative buckets have correct counts
    List<Long> negCounts = acc.getNegativeBuckets().getBucketCounts();
    assertThat(
            negCounts.get(valueToIndex(acc.getScale(), 0.1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            negCounts.get(valueToIndex(acc.getScale(), 1) - acc.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
  }
}
