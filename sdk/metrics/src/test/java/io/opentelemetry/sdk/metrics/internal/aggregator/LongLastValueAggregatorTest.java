/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongLastValueAggregator}. */
class LongLastValueAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final LongLastValueAggregator aggregator =
      new LongLastValueAggregator(ExemplarReservoir::longNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12L);
    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(14);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(14L);
  }

  @Test
  void aggregateThenMaybeReset() {
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordLong(13);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(13L);

    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12L);
  }

  @Test
  void diffInPlace() {
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
    MutableLongPointData pointData = (MutableLongPointData) aggregator.createReusablePoint();

    Attributes attributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<LongExemplarData> examplarsFrom =
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
    pointData.set(0, 1, attributes, 2000, examplarsFrom);

    MutableLongPointData toPointData = (MutableLongPointData) aggregator.createReusablePoint();

    Attributes toAttributes = Attributes.of(AttributeKey.longKey("test"), 100L);
    List<LongExemplarData> examplarsTo =
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
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    2, 100, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.CUMULATIVE);
    assertThat(metricData)
        .isEqualTo(
            ImmutableMetricData.createLongGauge(
                Resource.getDefault(),
                InstrumentationScopeInfo.empty(),
                "name",
                "description",
                "unit",
                ImmutableGaugeData.create(
                    Collections.singletonList(
                        ImmutableLongPointData.create(2, 100, Attributes.empty(), 10)))));
  }
}
