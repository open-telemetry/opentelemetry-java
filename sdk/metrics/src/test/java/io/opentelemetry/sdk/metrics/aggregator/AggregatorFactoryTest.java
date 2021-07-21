/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
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
    AggregatorFactory sum = AggregatorFactory.sum(AggregationTemporality.DELTA);
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

  @Test
  void getHistogramAggregatorFactory() {
    AggregatorFactory histogram =
        AggregatorFactory.histogram(Collections.singletonList(1.0), AggregationTemporality.DELTA);
    assertThat(
            histogram.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.VALUE_RECORDER,
                    InstrumentValueType.LONG)))
        .isInstanceOf(DoubleHistogramAggregator.class);
    assertThat(
            histogram.create(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                    "name",
                    "description",
                    "unit",
                    InstrumentType.VALUE_RECORDER,
                    InstrumentValueType.DOUBLE)))
        .isInstanceOf(DoubleHistogramAggregator.class);

    assertThat(
            histogram
                .create(
                    Resource.getDefault(),
                    InstrumentationLibraryInfo.empty(),
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.VALUE_RECORDER,
                        InstrumentValueType.LONG))
                .isStateful())
        .isFalse();
    assertThat(
            AggregatorFactory.histogram(
                    Collections.singletonList(1.0), AggregationTemporality.CUMULATIVE)
                .create(
                    Resource.getDefault(),
                    InstrumentationLibraryInfo.empty(),
                    InstrumentDescriptor.create(
                        "name",
                        "description",
                        "unit",
                        InstrumentType.VALUE_RECORDER,
                        InstrumentValueType.DOUBLE))
                .isStateful())
        .isTrue();

    assertThatThrownBy(
            () ->
                AggregatorFactory.histogram(
                    Collections.singletonList(Double.NEGATIVE_INFINITY),
                    AggregationTemporality.DELTA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: -Inf");
    assertThatThrownBy(
            () ->
                AggregatorFactory.histogram(
                    Arrays.asList(1.0, Double.POSITIVE_INFINITY), AggregationTemporality.DELTA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: +Inf");
    assertThatThrownBy(
            () ->
                AggregatorFactory.histogram(
                    Arrays.asList(1.0, Double.NaN), AggregationTemporality.DELTA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: NaN");
    assertThatThrownBy(
            () ->
                AggregatorFactory.histogram(
                    Arrays.asList(2.0, 1.0, 3.0), AggregationTemporality.DELTA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: 2.0 >= 1.0");
  }
}
