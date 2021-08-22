/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AggregatorHandle}. */
class DoubleLastValueAggregatorTest {
  private static final DoubleLastValueAggregator aggregator =
      new DoubleLastValueAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          MetricDescriptor.create("name", "description", "unit"));

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(DoubleLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(12.1);
    aggregatorHandle.recordDouble(13.1);
    aggregatorHandle.recordDouble(14.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(14.1);
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<Double> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(13.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(13.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(12.1);
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
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
        .hasDoubleGauge()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasAttributes(Attributes.empty())
                    .hasStartEpochNanos(0)
                    .hasEpochNanos(100)
                    .hasValue(10));
  }
}
