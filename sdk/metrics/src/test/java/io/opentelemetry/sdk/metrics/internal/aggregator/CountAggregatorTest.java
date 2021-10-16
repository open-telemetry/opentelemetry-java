/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CountAggregator}. */
class CountAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationLibraryInfo LIBRARY = InstrumentationLibraryInfo.empty();
  private static final MetricDescriptor SIMPLE_METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");

  private static final CountAggregator aggregator =
      new CountAggregator(
          ExemplarReservoir::noSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(CountAggregator.Handle.class);
  }

  @Test
  void toPoint() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void recordLongOperations() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(2);
  }

  @Test
  void recordDoubleOperations() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(12.3);
    aggregatorHandle.recordDouble(12.3);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(2);
  }

  @Test
  @SuppressWarnings("unchecked")
  void toMetricData_CumulativeTemporality() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
          RESOURCE, LIBRARY,
          SIMPLE_METRIC_DESCRIPTOR,
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
                AggregationTemporality.CUMULATIVE,
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
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
          RESOURCE, LIBRARY,
          SIMPLE_METRIC_DESCRIPTOR,
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
                AggregationTemporality.DELTA,
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
