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
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.EmptyExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.metrics.internal.exemplar.LongExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DoubleBase2ExponentialHistogramAggregatorTest {

  @Mock DoubleExemplarReservoir reservoir;
  private final ExemplarReservoirFactory reservoirFactory =
      new ExemplarReservoirFactory() {
        @Override
        public DoubleExemplarReservoir createDoubleExemplarReservoir() {
          return reservoir;
        }

        @Override
        public LongExemplarReservoir createLongExemplarReservoir() {
          throw new UnsupportedOperationException();
        }
      };

  private DoubleBase2ExponentialHistogramAggregator aggregator;

  private static final int MAX_SCALE = 20;
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");

  private static Stream<DoubleBase2ExponentialHistogramAggregator> provideAggregator() {
    List<DoubleBase2ExponentialHistogramAggregator> parameters = new ArrayList<>();
    for (MemoryMode memoryMode : MemoryMode.values()) {
      parameters.add(
          new DoubleBase2ExponentialHistogramAggregator(
              ExemplarReservoirFactory.noSamples(), 160, 20, memoryMode));
      parameters.add(
          new DoubleBase2ExponentialHistogramAggregator(
              ExemplarReservoirFactory.noSamples(), 160, MAX_SCALE, memoryMode));
    }
    return parameters.stream();
  }

  private static int valueToIndex(int scale, double value) {
    double scaleFactor = Math.scalb(1D / Math.log(2), scale);
    return (int) Math.ceil(Math.log(value) * scaleFactor) - 1;
  }

  private void initialize(MemoryMode memoryMode) {
    aggregator =
        new DoubleBase2ExponentialHistogramAggregator(
            ExemplarReservoirFactory.noSamples(), 160, 20, memoryMode);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void createHandle(MemoryMode memoryMode) {
    initialize(memoryMode);

    AggregatorHandle<?> handle = aggregator.createHandle();
    assertThat(handle).isInstanceOf(DoubleBase2ExponentialHistogramAggregator.Handle.class);
    ExponentialHistogramPointData point =
        ((DoubleBase2ExponentialHistogramAggregator.Handle) handle)
            .doAggregateThenMaybeResetDoubles(
                0, 1, Attributes.empty(), Collections.emptyList(), /* reset= */ true);
    assertThat(point.getPositiveBuckets()).isInstanceOf(EmptyExponentialHistogramBuckets.class);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(MAX_SCALE);
    assertThat(point.getNegativeBuckets()).isInstanceOf(EmptyExponentialHistogramBuckets.class);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(MAX_SCALE);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testRecordings(MemoryMode memoryMode) {
    initialize(memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(0.5, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(1.0, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(12.0, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(15.213, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(12.0, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(-13.2, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(-2.01, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(-1, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(0.0, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(0, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    List<Long> positiveCounts =
        Objects.requireNonNull(point).getPositiveBuckets().getBucketCounts();
    List<Long> negativeCounts = point.getNegativeBuckets().getBucketCounts();
    int expectedScale = 5; // should be downscaled from 20 to 5 after recordings

    assertThat(point.getScale()).isEqualTo(expectedScale);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(expectedScale);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(expectedScale);
    assertThat(point.getZeroCount()).isEqualTo(2);

    // Assert positive recordings are at correct index
    int posOffset = point.getPositiveBuckets().getOffset();
    assertThat(point.getPositiveBuckets().getTotalCount()).isEqualTo(5);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 0.5) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 1.0) - posOffset)).isEqualTo(1);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 12.0) - posOffset)).isEqualTo(2);
    assertThat(positiveCounts.get(valueToIndex(expectedScale, 15.213) - posOffset)).isEqualTo(1);

    // Assert negative recordings are at correct index
    int negOffset = point.getNegativeBuckets().getOffset();
    assertThat(point.getNegativeBuckets().getTotalCount()).isEqualTo(3);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 13.2) - negOffset)).isEqualTo(1);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 2.01) - negOffset)).isEqualTo(1);
    assertThat(negativeCounts.get(valueToIndex(expectedScale, 1.0) - negOffset)).isEqualTo(1);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testInvalidRecording(MemoryMode memoryMode) {
    initialize(memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = aggregator.createHandle();
    // Non-finite recordings should be ignored
    aggregatorHandle.recordDouble(Double.POSITIVE_INFINITY, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(Double.NEGATIVE_INFINITY, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(Double.NaN, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(Objects.requireNonNull(point).getSum()).isEqualTo(0);
    assertThat(point.getPositiveBuckets().getTotalCount()).isEqualTo(0);
    assertThat(point.getNegativeBuckets().getTotalCount()).isEqualTo(0);
    assertThat(point.getZeroCount()).isEqualTo(0);
  }

  @ParameterizedTest
  @MethodSource("provideAggregator")
  void testRecordingsAtLimits(DoubleBase2ExponentialHistogramAggregator aggregator) {
    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordDouble(Double.MIN_VALUE, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(Double.MAX_VALUE, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    List<Long> bucketCounts = Objects.requireNonNull(point).getPositiveBuckets().getBucketCounts();

    // assert buckets == [1 0 0 0 ... 1]
    assertThat(bucketCounts.get(0)).isEqualTo(1);
    assertThat(bucketCounts.get(bucketCounts.size() - 1)).isEqualTo(1);
    assertThat(bucketCounts.stream().filter(i -> i == 0).count())
        .isEqualTo(bucketCounts.size() - 2);
    assertThat(point.getPositiveBuckets().getTotalCount()).isEqualTo(2);

    // With 160 buckets allowed, minimum scale is -4
    assertThat(point.getScale()).isEqualTo(-4);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(-4);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(-4);

    // if scale is -4, base is 65,536.
    int base = 65_536;

    // Verify the rule holds:
    // base ^ (offset+i) <= (values recorded to bucket i) < base ^ (offset+i+1)

    // lowest bucket
    // As the bucket lower bound is less than Double.MIN_VALUE, Math.pow() rounds to 0
    assertThat(Math.pow(base, point.getPositiveBuckets().getOffset())).isEqualTo(0);
    assertThat(Math.pow(base, point.getPositiveBuckets().getOffset() + 1))
        .isGreaterThan(Double.MIN_VALUE);

    // highest bucket
    assertThat(Math.pow(base, point.getPositiveBuckets().getOffset() + bucketCounts.size() - 1))
        .isLessThanOrEqualTo(Double.MAX_VALUE);
    // As the bucket upper bound is greater than Double.MAX_VALUE, Math.pow() rounds to infinity
    assertThat(Math.pow(base, point.getPositiveBuckets().getOffset() + bucketCounts.size()))
        .isEqualTo(Double.POSITIVE_INFINITY);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void aggregateThenMaybeReset_WithExemplars(MemoryMode memoryMode) {
    DoubleBase2ExponentialHistogramAggregator agg =
        new DoubleBase2ExponentialHistogramAggregator(reservoirFactory, 160, MAX_SCALE, memoryMode);

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
    Mockito.when(reservoir.collectAndResetDoubles(Attributes.empty())).thenReturn(exemplars);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = agg.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());

    assertThat(
            Objects.requireNonNull(
                    aggregatorHandle.aggregateThenMaybeReset(
                        0, 1, Attributes.empty(), /* reset= */ true))
                .getExemplars())
        .isEqualTo(exemplars);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void aggregateThenMaybeReset(MemoryMode memoryMode) {
    initialize(memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordDouble(5.0, Attributes.empty(), Context.current());
    assertThat(
            Objects.requireNonNull(
                    aggregatorHandle.aggregateThenMaybeReset(
                        0, 1, Attributes.empty(), /* reset= */ true))
                .getPositiveBuckets()
                .getBucketCounts())
        .isEqualTo(Collections.singletonList(1L));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testInsert1M(MemoryMode memoryMode) {
    initialize(memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> handle = aggregator.createHandle();

    int n = 1024 * 1024 - 1;
    double min = 16.0 / n;
    double d = min;
    for (int i = 0; i < n; i++) {
      handle.recordDouble(d, Attributes.empty(), Context.current());
      d += min;
    }

    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true));
    assertThat(point.getScale()).isEqualTo(3);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(3);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(3);
    assertThat(point.getPositiveBuckets().getBucketCounts().size()).isEqualTo(160);
    assertThat(point.getPositiveBuckets().getTotalCount()).isEqualTo(n);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testDownScale(MemoryMode memoryMode) {
    initialize(memoryMode);

    DoubleBase2ExponentialHistogramAggregator.Handle handle =
        (DoubleBase2ExponentialHistogramAggregator.Handle) aggregator.createHandle();
    // record a measurement to initialize positive buckets
    handle.recordDouble(0.5, Attributes.empty(), Context.current());

    handle.downScale(20); // down to zero scale

    // test histogram operates properly after being manually scaled down to 0
    handle.recordDouble(1.0, Attributes.empty(), Context.current());
    handle.recordDouble(2.0, Attributes.empty(), Context.current());
    handle.recordDouble(4.0, Attributes.empty(), Context.current());
    handle.recordDouble(16.0, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true));
    assertThat(point.getScale()).isEqualTo(0);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(0);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(0);
    ExponentialHistogramBuckets buckets = point.getPositiveBuckets();
    assertThat(point.getSum()).isEqualTo(23.5);
    assertThat(buckets.getOffset()).isEqualTo(-2);
    assertThat(buckets.getBucketCounts()).isEqualTo(Arrays.asList(1L, 1L, 1L, 1L, 0L, 1L));
    assertThat(buckets.getTotalCount()).isEqualTo(5);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testToMetricData(MemoryMode memoryMode) {
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
    Mockito.when(reservoir.collectAndResetDoubles(Attributes.empty()))
        .thenReturn(Collections.singletonList(exemplar));

    DoubleBase2ExponentialHistogramAggregator cumulativeAggregator =
        new DoubleBase2ExponentialHistogramAggregator(reservoirFactory, 160, MAX_SCALE, memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle =
        cumulativeAggregator.createHandle();
    aggregatorHandle.recordDouble(0, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(0, Attributes.empty(), Context.current());
    aggregatorHandle.recordDouble(123.456, Attributes.empty(), Context.current());
    ExponentialHistogramPointData expPoint =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);

    MetricData metricDataCumulative =
        cumulativeAggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(expPoint),
            AggregationTemporality.CUMULATIVE);

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
      aggregatorHandle.recordDouble(1, Attributes.empty(), Context.current());
      aggregatorHandle.recordDouble(-1, Attributes.empty(), Context.current());
      aggregatorHandle.recordDouble(0, Attributes.empty(), Context.current());
    }

    MetricData metricDataDelta =
        cumulativeAggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(expPoint),
            AggregationTemporality.DELTA);
    assertThat(metricDataDelta.getType()).isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);
    assertThat(metricDataDelta.getExponentialHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void testMultithreadedUpdates(MemoryMode memoryMode) throws InterruptedException {
    initialize(memoryMode);

    AggregatorHandle<ExponentialHistogramPointData> aggregatorHandle = aggregator.createHandle();
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
                            aggregatorHandle.recordDouble(v, Attributes.empty(), Context.current());
                            if (ThreadLocalRandom.current().nextInt(10) == 0) {
                              aggregatorHandle.aggregateThenMaybeReset(
                                  0, 1, Attributes.empty(), /* reset= */ false);
                            }
                          }
                        }))
            .collect(Collectors.toList()));

    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false));
    assertThat(point.getZeroCount()).isEqualTo(numberOfUpdates);
    assertThat(point.getSum()).isCloseTo(100.0D * 10000, Offset.offset(0.0001)); // float error
    assertThat(point.getScale()).isEqualTo(3);
    assertThat(point.getPositiveBuckets().getScale()).isEqualTo(3);
    assertThat(point.getNegativeBuckets().getScale()).isEqualTo(3);
    ExponentialHistogramBuckets positiveBuckets = point.getPositiveBuckets();
    assertThat(positiveBuckets.getTotalCount()).isEqualTo(numberOfUpdates * 3);
    assertThat(positiveBuckets.getOffset()).isEqualTo(-27);
    ExponentialHistogramBuckets negativeBuckets = point.getNegativeBuckets();
    assertThat(negativeBuckets.getTotalCount()).isEqualTo(numberOfUpdates * 2);
    assertThat(negativeBuckets.getOffset()).isEqualTo(-27);

    // Verify positive buckets have correct counts
    List<Long> posCounts = point.getPositiveBuckets().getBucketCounts();
    assertThat(
            posCounts.get(
                valueToIndex(point.getScale(), 0.1) - point.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(
                valueToIndex(point.getScale(), 1) - point.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            posCounts.get(
                valueToIndex(point.getScale(), 100) - point.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);

    // Verify negative buckets have correct counts
    List<Long> negCounts = point.getNegativeBuckets().getBucketCounts();
    assertThat(
            negCounts.get(
                valueToIndex(point.getScale(), 0.1) - point.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
    assertThat(
            negCounts.get(
                valueToIndex(point.getScale(), 1) - point.getPositiveBuckets().getOffset()))
        .isEqualTo(numberOfUpdates);
  }

  @Test
  public void verifyMutableDataUsedInReusableDataMemoryMode() {
    initialize(MemoryMode.REUSABLE_DATA);

    DoubleBase2ExponentialHistogramAggregator.Handle handle =
        (DoubleBase2ExponentialHistogramAggregator.Handle) aggregator.createHandle();

    // record a measurement to initialize positive buckets
    handle.recordDouble(0.5, Attributes.empty(), Context.current());
    // record a measurement to initialize negative buckets
    handle.recordDouble(-13.2, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false));

    assertThat(point).isInstanceOf(MutableExponentialHistogramPointData.class);
    assertThat(point.getPositiveBuckets()).isInstanceOf(MutableExponentialHistogramBuckets.class);
    assertThat(point.getNegativeBuckets()).isInstanceOf(MutableExponentialHistogramBuckets.class);
    assertThat(point.getPositiveBuckets().getBucketCounts()).isNotEmpty();
    assertThat(point.getNegativeBuckets().getBucketCounts()).isNotEmpty();

    handle.recordDouble(0.6, Attributes.empty(), Context.current());
    handle.recordDouble(-16.3, Attributes.empty(), Context.current());

    ExponentialHistogramPointData secondAggregatePoint =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(1, 2, Attributes.empty(), /* reset= */ false));

    // Mutable point should be reused across collections.
    assertThat(secondAggregatePoint).isSameAs(point);
  }

  @Test
  public void verifyImmutableDataUsedInImmutableDataMemoryMode() {
    initialize(MemoryMode.IMMUTABLE_DATA);

    DoubleBase2ExponentialHistogramAggregator.Handle handle =
        (DoubleBase2ExponentialHistogramAggregator.Handle) aggregator.createHandle();

    // record a measurement to initialize positive buckets
    handle.recordDouble(0.5, Attributes.empty(), Context.current());
    // record a measurement to initialize negative buckets
    handle.recordDouble(-13.2, Attributes.empty(), Context.current());

    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false));

    assertThat(point).isInstanceOf(ImmutableExponentialHistogramPointData.class);
    assertThat(point.getPositiveBuckets())
        .isInstanceOf(DoubleBase2ExponentialHistogramBuckets.class);
    assertThat(point.getNegativeBuckets())
        .isInstanceOf(DoubleBase2ExponentialHistogramBuckets.class);
  }

  @Test
  public void reusablePoint_emptyFirstThenRecordAndCheck() {
    initialize(MemoryMode.REUSABLE_DATA);

    DoubleBase2ExponentialHistogramAggregator.Handle handle =
        (DoubleBase2ExponentialHistogramAggregator.Handle) aggregator.createHandle();

    // Let's create a point without buckets
    ExponentialHistogramPointData point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false));

    assertThat(point).isInstanceOf(MutableExponentialHistogramPointData.class);
    assertThat(point.getPositiveBuckets()).isInstanceOf(EmptyExponentialHistogramBuckets.class);
    assertThat(point.getNegativeBuckets()).isInstanceOf(EmptyExponentialHistogramBuckets.class);

    // record a measurement to initialize positive buckets
    handle.recordDouble(0.5, Attributes.empty(), Context.current());
    // record a measurement to initialize negative buckets
    handle.recordDouble(-13.2, Attributes.empty(), Context.current());

    point =
        Objects.requireNonNull(
            handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false));

    assertThat(point).isInstanceOf(MutableExponentialHistogramPointData.class);
    assertThat(point.getPositiveBuckets()).isInstanceOf(MutableExponentialHistogramBuckets.class);
    assertThat(point.getNegativeBuckets()).isInstanceOf(MutableExponentialHistogramBuckets.class);
    assertThat(point.getPositiveBuckets().getBucketCounts()).isNotEmpty();
    assertThat(point.getNegativeBuckets().getBucketCounts()).isNotEmpty();
  }
}
