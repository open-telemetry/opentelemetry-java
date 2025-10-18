/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.MutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.metrics.internal.exemplar.LongExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link LongSumAggregator}. */
@ExtendWith(MockitoExtension.class)
class LongSumAggregatorTest {

  @Mock LongExemplarReservoir reservoir;
  private final ExemplarReservoirFactory reservoirFactory =
      new ExemplarReservoirFactory() {
        @Override
        public DoubleExemplarReservoir createDoubleExemplarReservoir() {
          throw new UnsupportedOperationException();
        }

        @Override
        public LongExemplarReservoir createLongExemplarReservoir() {
          return reservoir;
        }
      };

  private static final Resource resource = Resource.getDefault();
  private static final InstrumentationScopeInfo library = InstrumentationScopeInfo.empty();
  private static final MetricDescriptor metricDescriptor =
      MetricDescriptor.create("name", "description", "unit");
  private LongSumAggregator aggregator;

  private void init(MemoryMode memoryMode) {
    aggregator =
        new LongSumAggregator(
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            ExemplarReservoirFactory.noSamples(),
            memoryMode);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void createHandle(MemoryMode memoryMode) {
    init(memoryMode);
    assertThat(aggregator.createHandle()).isInstanceOf(LongSumAggregator.Handle.class);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void multipleRecords(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12 * 5);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void multipleRecords_WithNegatives(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(-23, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(-11, Attributes.empty(), Context.current());
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(14);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void aggregateThenMaybeReset(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordLong(13, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(25);

    aggregatorHandle.recordLong(12, Attributes.empty(), Context.current());
    aggregatorHandle.recordLong(-25, Attributes.empty(), Context.current());
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(-13);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void aggregateThenMaybeReset_WithExemplars(MemoryMode memoryMode) {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    LongExemplarData exemplar =
        ImmutableLongExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<LongExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndResetLongs(Attributes.empty())).thenReturn(exemplars);
    LongSumAggregator aggregator =
        new LongSumAggregator(
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG,
                Advice.empty()),
            reservoirFactory,
            memoryMode);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(0, attributes, Context.root());
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(ImmutableLongPointData.create(0, 1, Attributes.empty(), 0, exemplars));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void mergeAndDiff(MemoryMode memoryMode) {
    LongExemplarData exemplar =
        ImmutableLongExemplarData.create(
            Attributes.empty(),
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<LongExemplarData> exemplars = Collections.singletonList(exemplar);
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        LongSumAggregator aggregator =
            new LongSumAggregator(
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    instrumentType,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                ExemplarReservoirFactory.noSamples(),
                memoryMode);

        LongPointData diffed =
            aggregator.diff(
                ImmutableLongPointData.create(0, 1, Attributes.empty(), 1L),
                ImmutableLongPointData.create(0, 1, Attributes.empty(), 2L, exemplars));
        assertThat(diffed.getValue())
            .withFailMessage(
                "Invalid diff result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, diffed)
            .isEqualTo(1);
        assertThat(diffed.getExemplars()).containsExactly(exemplar);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void diffInPlace(MemoryMode memoryMode) {
    init(memoryMode);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    LongExemplarData exemplar =
        ImmutableLongExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    List<LongExemplarData> exemplars = Collections.singletonList(exemplar);
    List<LongExemplarData> previousExemplars =
        Collections.singletonList(
            ImmutableLongExemplarData.create(
                attributes,
                1L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                2));

    MutableLongPointData previous = new MutableLongPointData();
    MutableLongPointData current = new MutableLongPointData();

    previous.set(0, 1, Attributes.empty(), 1, previousExemplars);
    current.set(0, 1, Attributes.empty(), 3, exemplars);

    aggregator.diffInPlace(previous, current);

    /* Assert that latest measurement is kept and set on {@code previous} */
    assertThat(previous.getStartEpochNanos()).isEqualTo(current.getStartEpochNanos());
    assertThat(previous.getEpochNanos()).isEqualTo(current.getEpochNanos());
    assertThat(previous.getAttributes()).isEqualTo(current.getAttributes());
    assertThat(previous.getValue()).isEqualTo(2);
    assertThat(previous.getExemplars()).isEqualTo(current.getExemplars());
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void copyPoint(MemoryMode memoryMode) {
    init(memoryMode);
    MutableLongPointData pointData = (MutableLongPointData) aggregator.createReusablePoint();

    Attributes attributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<LongExemplarData> exemplarsFrom =
        Collections.singletonList(
            ImmutableLongExemplarData.create(
                attributes,
                2L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                1));
    pointData.set(0, 1, attributes, 2000, exemplarsFrom);

    MutableLongPointData toPointData = (MutableLongPointData) aggregator.createReusablePoint();

    Attributes toAttributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<LongExemplarData> exemplarsTo =
        Collections.singletonList(
            ImmutableLongExemplarData.create(
                attributes,
                4L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                2));
    toPointData.set(0, 2, toAttributes, 4000, exemplarsTo);

    aggregator.copyPoint(pointData, toPointData);

    assertThat(toPointData.getStartEpochNanos()).isEqualTo(pointData.getStartEpochNanos());
    assertThat(toPointData.getEpochNanos()).isEqualTo(pointData.getEpochNanos());
    assertThat(toPointData.getAttributes()).isEqualTo(pointData.getAttributes());
    assertThat(toPointData.getValue()).isEqualTo(pointData.getValue());
    assertThat(toPointData.getExemplars()).isEqualTo(pointData.getExemplars());
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void toMetricData(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10, Attributes.empty(), Context.current());

    MetricData metricData =
        aggregator.toMetricData(
            resource,
            library,
            metricDescriptor,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    0, 100, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.CUMULATIVE);
    assertThat(metricData)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .isMonotonic()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(0)
                                .hasEpochNanos(100)
                                .hasAttributes(Attributes.empty())
                                .hasValue(10)));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void toMetricDataWithExemplars(MemoryMode memoryMode) {
    init(memoryMode);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    LongExemplarData exemplar =
        ImmutableLongExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);

    assertThat(
            aggregator.toMetricData(
                resource,
                library,
                metricDescriptor,
                Collections.singletonList(
                    ImmutableLongPointData.create(
                        0, 1, Attributes.empty(), 1, Collections.singletonList(exemplar))),
                AggregationTemporality.CUMULATIVE))
        .hasLongSumSatisfying(
            sum -> sum.hasPointsSatisfying(point -> point.hasValue(1).hasExemplars(exemplar)));
  }

  @Test
  void sameObjectReturnedOnReusableDataMemoryMode() {
    init(MemoryMode.REUSABLE_DATA);
    AggregatorHandle<LongPointData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordLong(1L, Attributes.empty(), Context.current());
    LongPointData firstCollection =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false);

    aggregatorHandle.recordLong(1L, Attributes.empty(), Context.current());
    LongPointData secondCollection =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false);

    // Should be same object since we are in REUSABLE_DATA mode.
    assertThat(firstCollection).isSameAs(secondCollection);
  }
}
