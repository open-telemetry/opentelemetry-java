/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link LongSumAggregator}. */
@ExtendWith(MockitoExtension.class)
class LongSumAggregatorTest {

  @Mock ExemplarReservoir<LongExemplarData> reservoir;

  private static final Resource resource = Resource.getDefault();
  private static final InstrumentationScopeInfo library = InstrumentationScopeInfo.empty();
  private static final MetricDescriptor metricDescriptor =
      MetricDescriptor.create("name", "description", "unit");
  private static final LongSumAggregator aggregator =
      new LongSumAggregator(
          InstrumentDescriptor.create(
              "instrument_name",
              "instrument_description",
              "instrument_unit",
              InstrumentType.COUNTER,
              InstrumentValueType.LONG),
          ExemplarReservoir::longNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongAccumulation, LongExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true).getValue())
        .isEqualTo(12 * 5);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isNull();
  }

  @Test
  void multipleRecords_WithNegatives() {
    AggregatorHandle<LongAccumulation, LongExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-23);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-11);
    assertThat(
            aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true).getValue())
        .isEqualTo(14);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isNull();
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<LongAccumulation, LongExemplarData> aggregatorHandle =
        aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isNull();

    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true).getValue())
        .isEqualTo(25);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isNull();

    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-25);
    assertThat(
            aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true).getValue())
        .isEqualTo(-13);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isNull();
  }

  @Test
  void testExemplarsInAccumulation() {
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
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);
    LongSumAggregator aggregator =
        new LongSumAggregator(
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG),
            () -> reservoir);
    AggregatorHandle<LongAccumulation, LongExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(0, attributes, Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true))
        .isEqualTo(LongAccumulation.create(0, exemplars));
  }

  @Test
  void mergeAndDiff() {
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
                    "name", "description", "unit", instrumentType, InstrumentValueType.LONG),
                ExemplarReservoir::longNoSamples);

        LongAccumulation diffed =
            aggregator.diff(LongAccumulation.create(1L), LongAccumulation.create(2L, exemplars));
        assertThat(diffed.getValue())
            .withFailMessage(
                "Invalid diff result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, diffed)
            .isEqualTo(1);
        assertThat(diffed.getExemplars()).containsExactly(exemplar);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData() {
    AggregatorHandle<LongAccumulation, LongExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            resource,
            library,
            metricDescriptor,
            Collections.singletonMap(
                Attributes.empty(),
                aggregatorHandle.accumulateThenReset(Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.CUMULATIVE,
            0,
            10,
            100);
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

  @Test
  void toMetricDataWithExemplars() {
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
    LongAccumulation accumulation = LongAccumulation.create(1, Collections.singletonList(exemplar));
    assertThat(
            aggregator.toMetricData(
                resource,
                library,
                metricDescriptor,
                Collections.singletonMap(Attributes.empty(), accumulation),
                AggregationTemporality.CUMULATIVE,
                0,
                10,
                100))
        .hasLongSumSatisfying(
            sum -> sum.hasPointsSatisfying(point -> point.hasValue(1).hasExemplars(exemplar)));
  }
}
