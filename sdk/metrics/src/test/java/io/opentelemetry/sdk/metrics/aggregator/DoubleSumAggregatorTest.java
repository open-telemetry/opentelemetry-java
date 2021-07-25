/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumAggregator}. */
class DoubleSumAggregatorTest {
  private static final DoubleSumAggregator aggregator =
      new DoubleSumAggregator(
          SumConfig.builder()
              .setName("name")
              .setDescription("description")
              .setUnit("unit")
              .setMonotonic(true)
              .setTemporality(AggregationTemporality.CUMULATIVE)
              .setMeasurementTemporality(AggregationTemporality.DELTA)
              .build(),
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          ExemplarSampler.ALWAYS_OFF);

  @Test
  void createStreamStorage() {
    assertThat(aggregator.createStreamStorage()).isInstanceOf(DoubleSumAggregator.MyHandle.class);
  }

  @Test
  void multipleRecords() {
    SynchronousHandle<DoubleAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordDouble(12.1, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12.1, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12.1, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12.1, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12.1, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(12.1 * 5));
  }

  @Test
  void multipleRecords_WithNegatives() {
    SynchronousHandle<DoubleAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(-23, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(-11, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(14));
  }

  @Test
  void toAccumulationAndReset() {
    SynchronousHandle<DoubleAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordDouble(13, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(25));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordDouble(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordDouble(-25, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(-13));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void merge() {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        DoubleSumAggregator aggregator =
            new DoubleSumAggregator(
                SumConfig.builder()
                    .setName("name")
                    .setDescription("description")
                    .setUnit("unit")
                    .setMonotonic(true)
                    .setTemporality(temporality)
                    .setMeasurementTemporality(
                        // TODO use: instrumentType
                        AggregationTemporality.DELTA)
                    .build(),
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                ExemplarSampler.ALWAYS_OFF);
        DoubleAccumulation merged = aggregator.merge(agg(1), agg(2));
        assertThat(merged.getValue())
            .withFailMessage(
                "Invalid merge result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, merged)
            .isEqualTo(3.0d);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void buildMetric() {
    SynchronousHandle<DoubleAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordDouble(10, Attributes.empty(), Context.root());

    MetricData metricData =
        aggregator.buildMetric(
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
            0,
            10,
            100);
    assertThat(metricData)
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

  private static DoubleAccumulation agg(double value) {
    return DoubleAccumulation.create(value);
  }
}
