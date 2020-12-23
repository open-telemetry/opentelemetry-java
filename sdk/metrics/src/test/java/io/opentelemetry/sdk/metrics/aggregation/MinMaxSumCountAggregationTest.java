/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MinMaxSumCountAggregationTest {

  @Test
  void toMetricData() {
    Aggregation<MinMaxSumCountAccumulation> minMaxSumCount =
        AggregationFactory.minMaxSumCount().create(InstrumentValueType.LONG);
    AggregatorHandle<MinMaxSumCountAccumulation> aggregatorHandle =
        minMaxSumCount.getAggregator().createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        minMaxSumCount.toMetricData(
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
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.SUMMARY);
  }

  @Test
  void getAggregatorFactory() {
    AggregationFactory minMaxSumCount = AggregationFactory.minMaxSumCount();
    assertThat(minMaxSumCount.create(InstrumentValueType.LONG).getAggregator())
        .isInstanceOf(LongMinMaxSumCountAggregator.getInstance().getClass());
    assertThat(minMaxSumCount.create(InstrumentValueType.DOUBLE).getAggregator())
        .isInstanceOf(DoubleMinMaxSumCountAggregator.getInstance().getClass());
  }
}
