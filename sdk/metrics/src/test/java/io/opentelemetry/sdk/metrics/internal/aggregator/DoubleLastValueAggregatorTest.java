/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.MutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Unit tests for {@link AggregatorHandle}. */
class DoubleLastValueAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private DoubleLastValueAggregator aggregator;

  private void init(MemoryMode memoryMode) {
    aggregator = new DoubleLastValueAggregator(ExemplarReservoir::doubleNoSamples, memoryMode);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void createHandle(MemoryMode memoryMode) {
    init(memoryMode);
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleLastValueAggregator.Handle.class);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void multipleRecords(MemoryMode memoryMode) {
    init(memoryMode);

    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(12.1);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12.1);
    aggregatorHandle.recordDouble(13.1);
    aggregatorHandle.recordDouble(14.1);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(14.1);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void aggregateThenMaybeReset(MemoryMode memoryMode) {
    init(memoryMode);

    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();

    aggregatorHandle.recordDouble(13.1);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(13.1);

    aggregatorHandle.recordDouble(12.1);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12.1);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void diff(MemoryMode memoryMode) {
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
    DoublePointData result =
        aggregator.diff(
            ImmutableDoublePointData.create(0, 1, Attributes.empty(), 1, previousExemplars),
            ImmutableDoublePointData.create(0, 1, Attributes.empty(), 2, exemplars));
    // Assert that latest measurement is kept.
    assertThat(result)
        .isEqualTo(ImmutableDoublePointData.create(0, 1, Attributes.empty(), 2, exemplars));
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
    current.set(0, 1, Attributes.empty(), 2, exemplars);

    aggregator.diffInPlace(previous, current);

    /* Assert that latest measurement is kept and set on {@code previous} */
    assertThat(previous.getStartEpochNanos()).isEqualTo(0);
    assertThat(previous.getEpochNanos()).isEqualTo(1);
    assertThat(previous.getAttributes()).isEqualTo(Attributes.empty());
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
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    10, 100, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.DELTA);
    assertThat(metricData)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleGaugeSatisfying(
            gauge ->
                gauge.hasPointsSatisfying(
                    point ->
                        point
                            .hasAttributes(Attributes.empty())
                            .hasStartEpochNanos(10)
                            .hasEpochNanos(100)
                            .hasValue(10)));
  }

  @Test
  void testReusableDataOnCollect() {
    init(MemoryMode.REUSABLE_DATA);
    AggregatorHandle<DoublePointData, DoubleExemplarData> handle = aggregator.createHandle();
    handle.recordDouble(1);
    DoublePointData pointData =
        handle.aggregateThenMaybeReset(0, 10, Attributes.empty(), /* reset= */ false);

    handle.recordDouble(1);
    DoublePointData pointData2 =
        handle.aggregateThenMaybeReset(0, 10, Attributes.empty(), /* reset= */ false);

    assertThat(pointData).isSameAs(pointData2);

    handle.recordDouble(1);
    DoublePointData pointDataWithReset =
        handle.aggregateThenMaybeReset(0, 10, Attributes.empty(), /* reset= */ true);

    assertThat(pointData).isSameAs(pointDataWithReset);
  }

  @Test
  void testNullPointerExceptionWhenUnset() {
    init(MemoryMode.REUSABLE_DATA);
    AggregatorHandle<DoublePointData, DoubleExemplarData> handle = aggregator.createHandle();
    assertThatThrownBy(
            () -> handle.aggregateThenMaybeReset(0, 10, Attributes.empty(), /* reset= */ true))
        .isInstanceOf(NullPointerException.class);
  }
}
