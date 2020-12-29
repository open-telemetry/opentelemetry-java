/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
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
  void toMetricData() {
    Aggregation<LongAccumulation> sum = AggregationFactory.sum().create(InstrumentValueType.LONG);
    AggregatorHandle<LongAccumulation> aggregatorHandle = sum.getAggregator().createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        sum.toMetricData(
            Resource.getDefault(),
            InstrumentationLibraryInfo.getEmpty(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.VALUE_RECORDER,
                InstrumentValueType.LONG),
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metricData.getLongSumData().getPoints()).hasSize(1);
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
