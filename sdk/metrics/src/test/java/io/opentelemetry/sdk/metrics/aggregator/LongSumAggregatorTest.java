/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AbstractSumAggregator.MergeStrategy;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongSumAggregator}. */
class LongSumAggregatorTest {
  private static final LongSumAggregator aggregator =
      new LongSumAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG),
          AggregationTemporality.CUMULATIVE);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(12 * 5);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void multipleRecords_WithNegatives() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-23);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-11);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(14);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(25);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(-25);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(-13);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void merge() {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        LongSumAggregator aggregator =
            new LongSumAggregator(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name", "description", "unit", instrumentType, InstrumentValueType.LONG),
                temporality);
        MergeStrategy expectedMergeStrategy =
            AbstractSumAggregator.resolveMergeStrategy(instrumentType, temporality);
        long merged = aggregator.merge(1L, 2L);
        assertThat(merged)
            .withFailMessage(
                "Invalid merge result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, merged)
            .isEqualTo(expectedMergeStrategy == MergeStrategy.SUM ? 3 : 1);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            10,
            100);
    assertThat(metricData)
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
}
