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
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
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

/** Unit tests for {@link DoubleSumAggregator}. */
@ExtendWith(MockitoExtension.class)
class DoubleSumAggregatorTest {

  @Mock ExemplarReservoir<DoubleExemplarData> reservoir;

  private static final Resource resource = Resource.getDefault();
  private static final InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private static final MetricDescriptor metricDescriptor =
      MetricDescriptor.create("name", "description", "unit");

  private static final DoubleSumAggregator aggregator =
      new DoubleSumAggregator(
          InstrumentDescriptor.create(
              "instrument_name",
              "instrument_description",
              "instrument_unit",
              InstrumentType.COUNTER,
              InstrumentValueType.DOUBLE),
          ExemplarReservoir::doubleNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
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

  @Test
  void multipleRecords_WithNegatives() {
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

  @Test
  void aggregateThenMaybeReset() {
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

  @Test
  void aggregateThenMaybeReset_WithExemplars() {
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
                InstrumentValueType.DOUBLE),
            () -> reservoir);
    AggregatorHandle<DoublePointData, DoubleExemplarData> aggregatorHandle =
        aggregator.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());
    assertThat(
            aggregatorHandle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true))
        .isEqualTo(ImmutableDoublePointData.create(0, 1, Attributes.empty(), 0, exemplars));
  }

  @Test
  void mergeAndDiff() {
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
                    "name", "description", "unit", instrumentType, InstrumentValueType.LONG),
                ExemplarReservoir::doubleNoSamples);

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

  @Test
  void toMetricData() {
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

  @Test
  void toMetricDataWithExemplars() {
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
}
