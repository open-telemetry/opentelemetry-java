/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class AggregatorFactoryTest {
  @Test
  void getCountAggregatorFactory() {
    AggregatorFactory count = AggregatorFactory.count(AggregationTemporality.CUMULATIVE);
    assertThat(
            count.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG)))
        .isInstanceOf(CountAggregator.class);
    assertThat(
            count.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE)))
        .isInstanceOf(CountAggregator.class);
  }

  @Test
  void getLastValueAggregatorFactory() {
    AggregatorFactory lastValue = AggregatorFactory.lastValue();
    assertThat(
            lastValue.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG)))
        .isInstanceOf(LongLastValueAggregator.class);
    assertThat(
            lastValue.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE)))
        .isInstanceOf(DoubleLastValueAggregator.class);
  }

  @Test
  void getMinMaxSumCountAggregatorFactory() {
    AggregatorFactory minMaxSumCount = AggregatorFactory.minMaxSumCount();
    assertThat(
            minMaxSumCount.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG)))
        .isInstanceOf(LongMinMaxSumCountAggregator.class);
    assertThat(
            minMaxSumCount.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE)))
        .isInstanceOf(DoubleMinMaxSumCountAggregator.class);
  }

  @Test
  void getSumAggregatorFactory() {
    AggregatorFactory sum = AggregatorFactory.sum(false);
    assertThat(
            sum.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.LONG)))
        .isInstanceOf(LongSumAggregator.class);
    assertThat(
            sum.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.COUNTER,
                    InstrumentValueType.DOUBLE)))
        .isInstanceOf(DoubleSumAggregator.class);
  }
}
