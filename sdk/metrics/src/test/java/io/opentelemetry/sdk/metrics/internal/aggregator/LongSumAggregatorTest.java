/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.aggregator.AbstractSumAggregator.MergeStrategy;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
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

  @Mock ExemplarReservoir reservoir;

  private static final LongSumAggregator aggregator =
      new LongSumAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "instrument_name",
              "instrument_description",
              "instrument_unit",
              InstrumentType.COUNTER,
              InstrumentValueType.LONG),
          MetricDescriptor.create("name", "description", "unit"),
          AggregationTemporality.CUMULATIVE,
          ExemplarReservoir::noSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue())
        .isEqualTo(12 * 5);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void multipleRecords_WithNegatives() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-23);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-11);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(14);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(25);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-25);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(-13);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void testExemplarsInAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Exemplar exemplar = DoubleExemplar.create(attributes, 2L, "spanid", "traceid", 1);
    List<Exemplar> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);
    LongSumAggregator aggregator =
        new LongSumAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            InstrumentDescriptor.create(
                "instrument_name",
                "instrument_description",
                "instrument_unit",
                InstrumentType.COUNTER,
                InstrumentValueType.LONG),
            MetricDescriptor.create("name", "description", "unit"),
            AggregationTemporality.CUMULATIVE,
            () -> reservoir);
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(0, attributes, Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()))
        .isEqualTo(LongAccumulation.create(0, exemplars));
  }

  @Test
  void merge() {
    Exemplar exemplar = DoubleExemplar.create(Attributes.empty(), 2L, "spanid", "traceid", 1);
    List<Exemplar> exemplars = Collections.singletonList(exemplar);
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        LongSumAggregator aggregator =
            new LongSumAggregator(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name", "description", "unit", instrumentType, InstrumentValueType.LONG),
                MetricDescriptor.create("name", "description", "unit"),
                temporality,
                ExemplarReservoir::noSamples);
        MergeStrategy expectedMergeStrategy =
            AbstractSumAggregator.resolveMergeStrategy(instrumentType, temporality);
        LongAccumulation merged =
            aggregator.merge(LongAccumulation.create(1L), LongAccumulation.create(2L, exemplars));
        assertThat(merged.getValue())
            .withFailMessage(
                "Invalid merge result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, merged)
            .isEqualTo(expectedMergeStrategy == MergeStrategy.SUM ? 3 : 1);
        assertThat(merged.getExemplars()).containsExactly(exemplar);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
            0,
            10,
            100);
    assertThat(metricData)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongSum()
        .isCumulative()
        .isMonotonic()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(0)
                    .hasEpochNanos(100)
                    .hasAttributes(Attributes.empty())
                    .hasValue(10));
  }

  @Test
  void toMetricDataWithExemplars() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Exemplar exemplar = DoubleExemplar.create(attributes, 2L, "spanid", "traceid", 1);
    LongAccumulation accumulation = LongAccumulation.create(1, Collections.singletonList(exemplar));
    assertThat(
            aggregator.toMetricData(
                Collections.singletonMap(Attributes.empty(), accumulation), 0, 10, 100))
        .hasLongSum()
        .points()
        .satisfiesExactly(point -> assertThat(point).hasValue(1).hasExemplars(exemplar));
  }
}
