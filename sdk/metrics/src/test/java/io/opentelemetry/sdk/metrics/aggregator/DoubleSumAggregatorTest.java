/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleSumAggregator}. */
class DoubleSumAggregatorTest {
  @Test
  void createHandle() {
    assertThat(DoubleSumAggregator.getInstance().createHandle())
        .isInstanceOf(DoubleSumAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<DoubleAccumulation> aggregatorHandle =
        DoubleSumAggregator.getInstance().createHandle();
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    aggregatorHandle.recordDouble(12.1);
    assertThat(aggregatorHandle.accumulateThenReset())
        .isEqualTo(DoubleAccumulation.create(12.1 * 5));
  }

  @Test
  void multipleRecords_WithNegatives() {
    AggregatorHandle<DoubleAccumulation> aggregatorHandle =
        DoubleSumAggregator.getInstance().createHandle();
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-23);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-11);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(14));
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<DoubleAccumulation> aggregatorHandle =
        DoubleSumAggregator.getInstance().createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(13);
    aggregatorHandle.recordDouble(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(25));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();

    aggregatorHandle.recordDouble(12);
    aggregatorHandle.recordDouble(-25);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(DoubleAccumulation.create(-13));
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void toMetricData() {
    Aggregator<DoubleAccumulation> sum = DoubleSumAggregator.getInstance();
    AggregatorHandle<DoubleAccumulation> aggregatorHandle = sum.createHandle();
    aggregatorHandle.recordDouble(10);

    MetricData metricData =
        sum.toMetricData(
            Resource.getDefault(),
            InstrumentationLibraryInfo.getEmpty(),
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE),
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            100);
    assertThat(metricData)
        .isEqualTo(
            MetricData.createDoubleSum(
                Resource.getDefault(),
                InstrumentationLibraryInfo.getEmpty(),
                "name",
                "description",
                "unit",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(0, 100, Labels.empty(), 10)))));
  }
}
