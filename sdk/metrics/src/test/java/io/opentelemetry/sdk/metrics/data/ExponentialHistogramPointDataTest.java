/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramBuckets;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ExponentialHistogramPointDataTest {

  @Test
  void create() {
    DoubleExemplarData doubleExemplarOne =
        ImmutableDoubleExemplarData.create(
            Attributes.empty(),
            0,
            SpanContext.create(
                "TraceId", "SpanId", TraceFlags.getDefault(), TraceState.getDefault()),
            1.0);

    DoubleExemplarData doubleExemplarTwo =
        ImmutableDoubleExemplarData.create(
            Attributes.empty(),
            2,
            SpanContext.create(
                "TraceId", "SpanId", TraceFlags.getDefault(), TraceState.getDefault()),
            2.0);
    ExponentialHistogramPointData pointData =
        ExponentialHistogramPointData.create(
            1,
            10.0,
            1,
            /* hasMin= */ true,
            2.0,
            /* hasMax= */ true,
            4.0,
            ImmutableExponentialHistogramBuckets.create(1, 10, Arrays.asList(1L, 2L)),
            ImmutableExponentialHistogramBuckets.create(1, 0, Collections.emptyList()),
            1,
            2,
            Attributes.empty(),
            Arrays.asList(doubleExemplarOne, doubleExemplarTwo));
    assertThat(pointData.getStartEpochNanos()).isEqualTo(1);
    assertThat(pointData.getEpochNanos()).isEqualTo(2);
    assertThat(pointData.getAttributes()).isEqualTo(Attributes.empty());
    assertThat(pointData.getSum()).isEqualTo(10.0);
    assertThat(pointData.getCount()).isEqualTo(4);
    assertThat(pointData.hasMin()).isTrue();
    assertThat(pointData.getMin()).isEqualTo(2.0);
    assertThat(pointData.hasMax()).isTrue();
    assertThat(pointData.getMax()).isEqualTo(4.0);
    assertThat(pointData.getPositiveBuckets().getTotalCount()).isEqualTo(3);
    assertThat(pointData.getNegativeBuckets().getTotalCount()).isEqualTo(0);
    assertThat(pointData.getPositiveBuckets().getBucketCounts()).containsExactly(1L, 2L);
    assertThat(pointData.getNegativeBuckets().getBucketCounts()).isEmpty();
  }
}
