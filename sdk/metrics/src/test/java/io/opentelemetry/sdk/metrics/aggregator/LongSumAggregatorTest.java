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

/** Unit tests for {@link LongSumAggregator}. */
class LongSumAggregatorTest {
  private static final LongSumAggregator aggregator =
      new LongSumAggregator(
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
    assertThat(aggregator.createStreamStorage()).isInstanceOf(LongSumAggregator.MyHandle.class);
  }

  @Test
  void multipleRecords() {
    SynchronousHandle<LongAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(12 * 5));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void multipleRecords_WithNegatives() {
    SynchronousHandle<LongAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(-23, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(-11, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(14));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void toAccumulationAndReset() {
    SynchronousHandle<LongAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(13, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(25));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(12, Attributes.empty(), Context.root());
    aggregatorHandle.recordLong(-25, Attributes.empty(), Context.root());
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isEqualTo(agg(-13));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void merge() {
    for (InstrumentType instrumentType : InstrumentType.values()) {
      for (AggregationTemporality temporality : AggregationTemporality.values()) {
        LongSumAggregator aggregator =
            new LongSumAggregator(
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
        LongAccumulation merged = aggregator.merge(agg(1L), agg(2L));
        assertThat(merged)
            .withFailMessage(
                "Invalid merge result for instrumentType %s, temporality %s: %s",
                instrumentType, temporality, merged)
            .isEqualTo(agg(3));
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void buildMetric() {
    SynchronousHandle<LongAccumulation> aggregatorHandle = aggregator.createStreamStorage();
    aggregatorHandle.recordLong(10, Attributes.empty(), Context.root());

    MetricData metricData =
        aggregator.buildMetric(
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
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

  private static LongAccumulation agg(long value) {
    return LongAccumulation.create(value);
  }
}
