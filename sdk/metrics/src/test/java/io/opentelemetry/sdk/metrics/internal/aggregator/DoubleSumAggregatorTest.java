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
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.MutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
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

/** Unit tests for {@link DoubleSumAggregator}. */
@ExtendWith(MockitoExtension.class)
class DoubleSumAggregatorTest {

  @Mock ExemplarReservoir<DoubleExemplarData> reservoir;

  private static final Resource resource = Resource.getDefault();
  private static final InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private static final MetricDescriptor metricDescriptor =
      MetricDescriptor.create("name", "description", "unit");

  private DoubleSumAggregator aggregator;

  private void init(MemoryMode memoryMode) {
    aggregator =
        new DoubleSumAggregator(
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()),
            ExemplarReservoir::doubleNoSamples,
            memoryMode);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void createHandle(MemoryMode memoryMode) {
    init(memoryMode);
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleSumAggregator.Handle.class);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void multipleRecords(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12.1 * 5);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void multipleRecords_WithNegatives(MemoryMode memoryMode) {
    init(memoryMode);
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-23);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-11);
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
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();

    aggregatorHandle.recordDouble(13);
    aggregatorHandle.recordDouble(12);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(25);

    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-25);
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
    DoubleSumAggregator aggregator =
        new DoubleSumAggregator(
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.DOUBLE,
                Advice.empty()),
            () -> reservoir,
            memoryMode);
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(ImmutableDoublePointData.create(0, 1, Attributes.empty(), 0, exemplars));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void mergeAndDiff(MemoryMode memoryMode) {
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
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        DoubleSumAggregator aggregator =
            new DoubleSumAggregator(
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    instrumentType,
                    InstrumentValueType.LONG,
                    Advice.empty()),
                ExemplarReservoir::doubleNoSamples,
                memoryMode);

        DoublePointData diffed =
            aggregator.diff(
                ImmutableDoublePointData.create(0, 1, Attributes.empty(), 1d),
                ImmutableDoublePointData.create(0, 1, Attributes.empty(), 2d, exemplars));
        assertThat(diffed.getValue())
            .withFailMessage(
                "Invalid diff result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, diffed)
            .isEqualTo(1d);
        assertThat(diffed.getExemplars()).containsExactly(exemplar);
      }
    }
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void diffInPlace(MemoryMode memoryMode) {
    init(memoryMode);
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
    List<DoubleExemplarData> previousExemplars =
        Collections.singletonList(
            ImmutableDoubleExemplarData.create(
                attributes,
                1L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                2));

    MutableDoublePointData previous = new MutableDoublePointData();
    MutableDoublePointData current = new MutableDoublePointData();

    previous.set(0, 1, Attributes.empty(), 1, previousExemplars);
    current.set(0, 1, Attributes.empty(), 3, exemplars);

    aggregator.diffInPlace(previous, current);

    /* Assert that latest measurement is kept and set on {@code previous} */
    assertThat(previous.getStartEpochNanos()).isEqualTo(current.getStartEpochNanos());
    assertThat(previous.getEpochNanos()).isEqualTo(current.getEpochNanos());
    assertThat(previous.getAttributes()).isEqualTo(current.getAttributes());
    assertThat(previous.getValue()).isEqualTo(2);
    assertThat(previous.getExemplars()).isEqualTo(exemplars);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void copyPoint(MemoryMode memoryMode) {
    init(memoryMode);
    MutableDoublePointData pointData = (MutableDoublePointData) aggregator.createReusablePoint();

    Attributes attributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<DoubleExemplarData> exemplarsFrom =
        Collections.singletonList(
            ImmutableDoubleExemplarData.create(
                attributes,
                2L,
                SpanContext.create(
                    "00000000000000000000000000000001",
                    "0000000000000002",
                    TraceFlags.getDefault(),
                    TraceState.getDefault()),
                1));
    pointData.set(0, 1, attributes, 2000, exemplarsFrom);

    MutableDoublePointData toPointData = (MutableDoublePointData) aggregator.createReusablePoint();

    Attributes toAttributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<DoubleExemplarData> exemplarsTo =
        Collections.singletonList(
            ImmutableDoubleExemplarData.create(
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
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(10);

    MetricData metricData =
        aggregator.toMetricData(
            resource,
            scope,
            metricDescriptor,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    0, 100, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.CUMULATIVE);
    assertThat(metricData)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleSumSatisfying(
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
    assertThat(
            aggregator.toMetricData(
                resource,
                scope,
                metricDescriptor,
                Collections.singletonList(
                    ImmutableDoublePointData.create(
                        0, 1, Attributes.empty(), 1, Collections.singletonList(exemplar))),
                AggregationTemporality.CUMULATIVE))
        .hasDoubleSumSatisfying(
            sum -> sum.hasPointsSatisfying(point -> point.hasValue(1).hasExemplars(exemplar)));
  }

  @Test
  void sameObjectReturnedOnReusableDataMemoryMode() {
    init(MemoryMode.REUSABLE_DATA);
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(1.0);

    DoublePointData firstCollection =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false);

    aggregatorHandle.recordDouble(1.0);
    DoublePointData secondCollection =
        aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ false);

    // Should be same object since we are in REUSABLE_DATA mode.
    assertThat(firstCollection).isSameAs(secondCollection);
  }
}
