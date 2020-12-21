/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.CountAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Aggregations#count()}. */
class CountAggregationTest {
  @Test
  void toMetricData() {
    Aggregation count = Aggregations.count();
    Aggregator aggregator = count.getAggregatorFactory(InstrumentValueType.LONG).getAggregator();
    aggregator.recordLong(10);

    MetricData metricData =
        count.toMetricData(
            Resource.getDefault(),
            InstrumentationLibraryInfo.getEmpty(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.VALUE_RECORDER,
                InstrumentValueType.LONG),
            Collections.singletonMap(Labels.empty(), aggregator.toAccumulationThenReset()),
            0,
            100);
    assertThat(metricData).isNotNull();
    assertThat(metricData.getUnit()).isEqualTo("1");
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_SUM);
  }

  @Test
  void getAggregatorFactory() {
    Aggregation count = Aggregations.count();
    assertThat(count.getAggregatorFactory(InstrumentValueType.LONG))
        .isInstanceOf(CountAggregator.getFactory().getClass());
    assertThat(count.getAggregatorFactory(InstrumentValueType.DOUBLE))
        .isInstanceOf(CountAggregator.getFactory().getClass());
  }
}
