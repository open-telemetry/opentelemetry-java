/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Simple tests of Aggregation classes API. */
class AggregationTest {

  @Test
  void haveToString() {
    assertThat(Aggregation.drop()).asString().contains("DropAggregation");
    assertThat(Aggregation.defaultAggregation()).asString().contains("Default");
    assertThat(Aggregation.lastValue()).asString().contains("LastValue");
    assertThat(Aggregation.sum()).asString().contains("Sum");
    assertThat(Aggregation.explicitBucketHistogram())
        .asString()
        .contains("ExplicitBucketHistogramAggregation");
    assertThat(Aggregation.explicitBucketHistogram(Collections.singletonList(1.0d)))
        .asString()
        .contains("ExplicitBucketHistogramAggregation");
    assertThat(Aggregation.base2ExponentialBucketHistogram())
        .asString()
        .isEqualTo(
            "Base2ExponentialHistogramAggregation{maxBuckets=160,maxScale=20,recordMinMax=true}");
    assertThat(Aggregation.base2ExponentialBucketHistogram(2, 0))
        .asString()
        .isEqualTo(
            "Base2ExponentialHistogramAggregation{maxBuckets=2,maxScale=0,recordMinMax=true}");
  }

  @Test
  void histogramUsesExplicitBucket() {
    assertThat(Aggregation.explicitBucketHistogram())
        .asString()
        .contains("ExplicitBucketHistogram");
  }

  @Test
  void aggregationIsCompatible() {
    InstrumentDescriptor counter = descriptorForType(InstrumentType.COUNTER);
    InstrumentDescriptor observableCounter = descriptorForType(InstrumentType.OBSERVABLE_COUNTER);
    InstrumentDescriptor upDownCounter = descriptorForType(InstrumentType.UP_DOWN_COUNTER);
    InstrumentDescriptor observableUpDownCounter =
        descriptorForType(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER);
    InstrumentDescriptor observableGauge = descriptorForType(InstrumentType.OBSERVABLE_GAUGE);
    InstrumentDescriptor gauge = descriptorForType(InstrumentType.GAUGE);
    InstrumentDescriptor histogram = descriptorForType(InstrumentType.HISTOGRAM);

    AggregatorFactory defaultAggregation = ((AggregatorFactory) Aggregation.defaultAggregation());
    assertThat(defaultAggregation.isCompatibleWithInstrument(counter)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(observableCounter)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(gauge)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(upDownCounter)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(observableUpDownCounter)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(observableGauge)).isTrue();
    assertThat(defaultAggregation.isCompatibleWithInstrument(histogram)).isTrue();

    AggregatorFactory drop = ((AggregatorFactory) Aggregation.drop());
    assertThat(drop.isCompatibleWithInstrument(counter)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(observableCounter)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(gauge)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(upDownCounter)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(observableUpDownCounter)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(observableGauge)).isTrue();
    assertThat(drop.isCompatibleWithInstrument(histogram)).isTrue();

    AggregatorFactory sum = ((AggregatorFactory) Aggregation.sum());
    assertThat(sum.isCompatibleWithInstrument(counter)).isTrue();
    assertThat(sum.isCompatibleWithInstrument(observableCounter)).isTrue();
    assertThat(sum.isCompatibleWithInstrument(upDownCounter)).isTrue();
    assertThat(sum.isCompatibleWithInstrument(observableUpDownCounter)).isTrue();
    assertThat(sum.isCompatibleWithInstrument(observableGauge)).isFalse();
    assertThat(sum.isCompatibleWithInstrument(gauge)).isFalse();
    assertThat(sum.isCompatibleWithInstrument(histogram)).isTrue();

    AggregatorFactory explicitHistogram =
        ((AggregatorFactory) Aggregation.explicitBucketHistogram());
    assertThat(explicitHistogram.isCompatibleWithInstrument(counter)).isTrue();
    assertThat(explicitHistogram.isCompatibleWithInstrument(observableCounter)).isFalse();
    assertThat(explicitHistogram.isCompatibleWithInstrument(upDownCounter)).isFalse();
    assertThat(explicitHistogram.isCompatibleWithInstrument(observableUpDownCounter)).isFalse();
    assertThat(explicitHistogram.isCompatibleWithInstrument(observableGauge)).isFalse();
    assertThat(explicitHistogram.isCompatibleWithInstrument(gauge)).isFalse();
    assertThat(explicitHistogram.isCompatibleWithInstrument(histogram)).isTrue();

    AggregatorFactory exponentialHistogram =
        ((AggregatorFactory) Aggregation.base2ExponentialBucketHistogram());
    assertThat(exponentialHistogram.isCompatibleWithInstrument(counter)).isTrue();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(observableCounter)).isFalse();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(upDownCounter)).isFalse();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(observableUpDownCounter)).isFalse();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(observableGauge)).isFalse();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(gauge)).isFalse();
    assertThat(exponentialHistogram.isCompatibleWithInstrument(histogram)).isTrue();

    AggregatorFactory lastValue = ((AggregatorFactory) Aggregation.lastValue());
    assertThat(lastValue.isCompatibleWithInstrument(counter)).isFalse();
    assertThat(lastValue.isCompatibleWithInstrument(observableCounter)).isFalse();
    assertThat(lastValue.isCompatibleWithInstrument(upDownCounter)).isFalse();
    assertThat(lastValue.isCompatibleWithInstrument(observableUpDownCounter)).isFalse();
    assertThat(lastValue.isCompatibleWithInstrument(observableGauge)).isTrue();
    assertThat(lastValue.isCompatibleWithInstrument(gauge)).isTrue();
    assertThat(lastValue.isCompatibleWithInstrument(histogram)).isFalse();
  }

  private static InstrumentDescriptor descriptorForType(InstrumentType instrumentType) {
    return InstrumentDescriptor.create(
        "name", "description", "unit", instrumentType, InstrumentValueType.DOUBLE, Advice.empty());
  }
}
