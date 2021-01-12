/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongSumAggregator}. */
class LongSumAggregatorTest {
  @Test
  void createHandle() {
    assertThat(LongSumAggregator.getInstance().createHandle())
        .isInstanceOf(LongSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<Long> aggregatorHandle = LongSumAggregator.getInstance().createHandle();
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
    AggregatorHandle<Long> aggregatorHandle = LongSumAggregator.getInstance().createHandle();
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
    AggregatorHandle<Long> aggregatorHandle = LongSumAggregator.getInstance().createHandle();
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
  void toMetricData() {
    Aggregator<Long> sum = LongSumAggregator.getInstance();
    AggregatorHandle<Long> aggregatorHandle = sum.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        sum.toMetricData(
            Resource.getDefault(),
            InstrumentationLibraryInfo.getEmpty(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG),
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            100);
    assertThat(metricData)
        .isEqualTo(
            MetricData.createLongSum(
                Resource.getDefault(),
                InstrumentationLibraryInfo.getEmpty(),
                "name",
                "description",
                "unit",
                LongSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(LongPoint.create(0, 100, Labels.empty(), 10)))));
  }
}
