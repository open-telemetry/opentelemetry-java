/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
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
    Aggregation lastValue = AggregationFactory.lastValue().create(InstrumentValueType.LONG);
    Aggregator<?> aggregator = lastValue.getAggregatorFactory().getAggregator();
    aggregator.recordLong(10);

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
            Collections.singletonMap(Labels.empty(), aggregator.accumulateThenReset()),
            0,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_GAUGE);
  }

  @Test
  void getAggregatorFactory() {
    AggregationFactory lastValue = AggregationFactory.lastValue();
    assertThat(lastValue.create(InstrumentValueType.LONG).getAggregatorFactory())
        .isInstanceOf(LongLastValueAggregator.getFactory().getClass());
    assertThat(lastValue.create(InstrumentValueType.DOUBLE).getAggregatorFactory())
        .isInstanceOf(DoubleLastValueAggregator.getFactory().getClass());
  }
}
