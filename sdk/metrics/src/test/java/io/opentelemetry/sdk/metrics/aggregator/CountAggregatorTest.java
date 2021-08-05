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
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CountAggregator}. */
class CountAggregatorTest {
  private static final CountAggregator cumulativeAggregator =
      new CountAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "name",
              "description",
              "unit",
              InstrumentType.VALUE_RECORDER,
              InstrumentValueType.LONG),
          AggregationTemporality.CUMULATIVE);
  private static final CountAggregator deltaAggregator =
      new CountAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          InstrumentDescriptor.create(
              "name",
              "description",
              "unit",
              InstrumentType.VALUE_RECORDER,
              InstrumentValueType.LONG),
          AggregationTemporality.DELTA);

  @Test
  void createHandle() {
    assertThat(cumulativeAggregator.createHandle()).isInstanceOf(CountAggregator.Handle.class);
  }

  @Test
  void toPoint() {
    AggregatorHandle<Long> aggregatorHandle = cumulativeAggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void recordLongOperations() {
    AggregatorHandle<Long> aggregatorHandle = cumulativeAggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(2);
  }

  @Test
  void recordDoubleOperations() {
    AggregatorHandle<Long> aggregatorHandle = cumulativeAggregator.createHandle();
    aggregatorHandle.recordDouble(12.3);
    aggregatorHandle.recordDouble(12.3);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(2);
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData_CumulativeTemporality() {
    AggregatorHandle<Long> aggregatorHandle = cumulativeAggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        cumulativeAggregator.toMetricData(
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            10,
            100);
    assertThat(metricData)
        .hasResource(Resource.getDefault())
        .hasInstrumentationLibrary(InstrumentationLibraryInfo.empty())
        .hasName("name")
        .hasDescription("description")
        .hasUnit("1")
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
                    .hasValue(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData_DeltaTemporality() {
    AggregatorHandle<Long> aggregatorHandle = deltaAggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        deltaAggregator.toMetricData(
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            10,
            100);
    assertThat(metricData)
        .hasResource(Resource.getDefault())
        .hasInstrumentationLibrary(InstrumentationLibraryInfo.empty())
        .hasName("name")
        .hasDescription("description")
        .hasUnit("1")
        .hasLongSum()
        .isDelta()
        .isMonotonic()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(10)
                    .hasEpochNanos(100)
                    .hasAttributes(Attributes.empty())
                    .hasValue(1));
  }
}
