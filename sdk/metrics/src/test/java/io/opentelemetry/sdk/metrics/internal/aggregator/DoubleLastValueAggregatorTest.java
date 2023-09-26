/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
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

/** Unit tests for {@link AggregatorHandle}. */
class DoubleLastValueAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final DoubleLastValueAggregator aggregator =
      new DoubleLastValueAggregator(ExemplarReservoir::doubleNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
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

  @Test
  void aggregateThenMaybeReset() {
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

  @Test
  void diff() {
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

  @Test
  void diffInPlace() {
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

  @Test
  void copyPoint() {
    MutableDoublePointData pointData = (MutableDoublePointData) aggregator.createReusablePoint();

    Attributes attributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<DoubleExemplarData> examplarsFrom =
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
    pointData.set(0, 1, attributes, 2000, examplarsFrom);

    MutableDoublePointData toPointData = (MutableDoublePointData) aggregator.createReusablePoint();

    Attributes toAttributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<DoubleExemplarData> examplarsTo =
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
    toPointData.set(0, 2, toAttributes, 4000, examplarsTo);

    aggregator.copyPoint(pointData, toPointData);

    assertThat(toPointData.getStartEpochNanos()).isEqualTo(pointData.getStartEpochNanos());
    assertThat(toPointData.getEpochNanos()).isEqualTo(pointData.getEpochNanos());
    assertThat(toPointData.getAttributes()).isEqualTo(pointData.getAttributes());
    assertThat(toPointData.getValue()).isEqualTo(pointData.getValue());
    assertThat(toPointData.getExemplars()).isEqualTo(pointData.getExemplars());
  }

  @Test
  void toMetricData() {
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
}
