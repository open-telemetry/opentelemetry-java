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
import org.junit.jupiter.api.Assertions;
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

  @Test
  void getHistogramAggregatorFactory() {
    AggregatorFactory histogram =
        AggregatorFactory.histogram(new double[] {1.0}, /* stateful= */ false);
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
            AggregatorFactory.histogram(new double[] {1.0}, /* stateful= */ true)
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

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            AggregatorFactory.histogram(
                new double[] {Double.NEGATIVE_INFINITY}, /* stateful= */ false));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            AggregatorFactory.histogram(
                new double[] {1, Double.POSITIVE_INFINITY}, /* stateful= */ false));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> AggregatorFactory.histogram(new double[] {2, 1, 3}, /* stateful= */ false));
  }
}
