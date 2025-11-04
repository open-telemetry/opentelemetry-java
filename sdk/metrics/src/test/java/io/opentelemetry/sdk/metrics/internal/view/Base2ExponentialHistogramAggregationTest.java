/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal.asExemplarFilterInternal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import org.junit.jupiter.api.Test;

class Base2ExponentialHistogramAggregationTest {

  @Test
  void goodConfig() {
    assertThat(Base2ExponentialHistogramAggregation.getDefault()).isNotNull();
    assertThat(Base2ExponentialHistogramAggregation.create(10, 20)).isNotNull();
  }

  @Test
  void invalidConfig_Throws() {
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(0, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxBuckets must be >= 2");
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(2, 21))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(2, -11))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
  }

  @Test
  void minimumBucketsCanAccommodateMaxRange() {
    Aggregation aggregation = Base2ExponentialHistogramAggregation.create(2, 20);
    Aggregator<ExponentialHistogramPointData> aggregator =
        ((AggregatorFactory) aggregation)
            .createAggregator(
                InstrumentDescriptor.create(
                    "foo",
                    "description",
                    "unit",
                    InstrumentType.HISTOGRAM,
                    InstrumentValueType.DOUBLE,
                    Advice.empty()),
                asExemplarFilterInternal(ExemplarFilter.alwaysOff()),
                MemoryMode.IMMUTABLE_DATA);
    AggregatorHandle<ExponentialHistogramPointData> handle = aggregator.createHandle();
    // Record max range
    handle.recordDouble(Double.MIN_VALUE, Attributes.empty(), Context.current());
    handle.recordDouble(Double.MAX_VALUE, Attributes.empty(), Context.current());

    ExponentialHistogramPointData pointData =
        handle.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(pointData.getCount()).isEqualTo(2);
    assertThat(pointData.getScale()).isEqualTo(-11);
  }
}
