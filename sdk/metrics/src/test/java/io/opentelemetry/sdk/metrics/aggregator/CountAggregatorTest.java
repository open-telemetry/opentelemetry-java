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
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CountAggregator}. */
class CountAggregatorTest {
  private static final CountAggregator aggregator =
      new CountAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.getEmpty(),
          InstrumentDescriptor.create(
              "name",
              "description",
              "unit",
              InstrumentType.VALUE_RECORDER,
              InstrumentValueType.LONG));

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(CountAggregator.Handle.class);
  }

  @Test
  void toPoint() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset()).isNull();
  }

  @Test
  void recordLongOperations() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(2);
  }

  @Test
  void recordDoubleOperations() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(12.3);
    aggregatorHandle.recordDouble(12.3);
    assertThat(aggregatorHandle.accumulateThenReset()).isEqualTo(2);
  }

  @Test
  void toMetricData() {
    AggregatorHandle<Long> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
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
                "1",
                LongSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(LongPointData.create(0, 100, Labels.empty(), 1)))));
  }
}
