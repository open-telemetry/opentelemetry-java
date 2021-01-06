/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.accumulation.LongAccumulation;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AggregationFactory#sum()}. */
class SumAggregationTest {

  @Test
  void toDoubleMetricData() {
    Aggregation<DoubleAccumulation> sum = DoubleSumAggregation.INSTANCE;
    AggregatorHandle<DoubleAccumulation> aggregatorHandle = sum.getAggregator().createHandle();
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
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.DOUBLE_SUM);
    assertThat(metricData.getDoubleSumData().getPoints())
        .containsExactly(MetricData.DoublePoint.create(0, 100, Labels.empty(), 10));
  }

  @Test
  void toLongMetricData() {
    Aggregation<LongAccumulation> sum = LongSumAggregation.INSTANCE;
    AggregatorHandle<LongAccumulation> aggregatorHandle = sum.getAggregator().createHandle();
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
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metricData.getLongSumData().getPoints())
        .containsExactly(MetricData.LongPoint.create(0, 100, Labels.empty(), 10));
  }

  @Test
  void getAggregatorFactory() {
    AggregationFactory sum = AggregationFactory.sum();
    assertThat(sum.create(InstrumentValueType.LONG).getAggregator())
        .isInstanceOf(LongSumAggregator.getInstance().getClass());
    assertThat(sum.create(InstrumentValueType.DOUBLE).getAggregator())
        .isInstanceOf(DoubleSumAggregator.getInstance().getClass());
  }
}
