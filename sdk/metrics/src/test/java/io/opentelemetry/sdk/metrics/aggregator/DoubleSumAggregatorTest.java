/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AbstractSumAggregator.MergeStrategy;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumAggregator}. */
class DoubleSumAggregatorTest {
  private static final DoubleSumAggregator aggregator =
      new DoubleSumAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "instrument_name",
              "instrument_description",
              "instrument_unit",
              InstrumentType.COUNTER,
              InstrumentValueType.DOUBLE),
          MetricDescriptor.create("name", "description", "unit"),
          AggregationTemporality.CUMULATIVE);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(12.1 * 5);
  }

  @Test
  void multipleRecords_WithNegatives() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-23);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-11);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(14);
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(13);
    aggregatorHandle.recordDouble(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(25);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-25);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(-13);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void merge() {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        DoubleSumAggregator aggregator =
            new DoubleSumAggregator(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name", "description", "unit", instrumentType, InstrumentValueType.LONG),
                MetricDescriptor.create("name", "description", "unit"),
                temporality);
        MergeStrategy expectedMergeStrategy =
            AbstractSumAggregator.resolveMergeStrategy(instrumentType, temporality);
        double merged = aggregator.merge(1.0d, 2.0d);
        assertThat(merged)
            .withFailMessage(
                "Invalid merge result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, merged)
            .isEqualTo(expectedMergeStrategy == MergeStrategy.SUM ? 3.0d : 1.0d);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(10);

    MetricData metricData =
        aggregator.toMetricData(
            Collections.singletonMap(Attributes.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            10,
            100);
    assertThat(metricData)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleSum()
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
}
