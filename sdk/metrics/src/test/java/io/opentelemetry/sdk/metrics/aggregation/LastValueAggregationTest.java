/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LastValueAggregationTest {

  @Test
  void toMetricData() {
    Aggregation<LongAccumulation> lastValue =
        AggregationFactory.lastValue().create(InstrumentValueType.LONG);
    AggregatorHandle<LongAccumulation> aggregatorHandle = lastValue.getAggregator().createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        lastValue.toMetricData(
            Resource.getDefault(),
            InstrumentationLibraryInfo.getEmpty(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.VALUE_OBSERVER,
                InstrumentValueType.LONG),
            Collections.singletonMap(Labels.empty(), aggregatorHandle.accumulateThenReset()),
            0,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_GAUGE);
  }

  @Test
  void getAggregatorFactory() {
    AggregationFactory lastValue = AggregationFactory.lastValue();
    assertThat(lastValue.create(InstrumentValueType.LONG).getAggregator())
        .isInstanceOf(Aggregator.longLastValue().getClass());
    assertThat(lastValue.create(InstrumentValueType.DOUBLE).getAggregator())
        .isInstanceOf(Aggregator.doubleLastValue().getClass());
  }
}
