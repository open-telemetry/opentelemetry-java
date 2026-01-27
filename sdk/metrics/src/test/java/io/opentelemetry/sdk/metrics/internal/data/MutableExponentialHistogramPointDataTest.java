/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MutableExponentialHistogramPointDataTest {

  @Test
  public void testSanity() {
    MutableExponentialHistogramPointData pointData = new MutableExponentialHistogramPointData();
    assertThat(pointData.getSum()).isEqualTo(0);
    assertThat(pointData.getCount()).isEqualTo(0);
    assertThat(pointData.getPositiveBuckets().getTotalCount()).isEqualTo(0);
    assertThat(pointData.getNegativeBuckets().getTotalCount()).isEqualTo(0);
    assertThat(pointData.getExemplars()).isEmpty();

    MutableExponentialHistogramBuckets positiveBuckets = new MutableExponentialHistogramBuckets();
    positiveBuckets.set(
        /* scale= */ 1, /* offset= */ 2, /* totalCount= */ 3, DynamicPrimitiveLongList.of(1, 2, 3));
    MutableExponentialHistogramBuckets negativeBuckets = new MutableExponentialHistogramBuckets();
    negativeBuckets.set(10, 20, 30, DynamicPrimitiveLongList.of(50, 60, 70));

    pointData.set(
        /* scale= */ 1,
        /* sum= */ 2,
        /* zeroCount= */ 10,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        positiveBuckets,
        negativeBuckets,
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        Collections.emptyList());

    assertThat(pointData.getSum()).isEqualTo(2);
    assertThat(pointData.getCount()).isEqualTo(10 + 30 + 3);
    assertThat(pointData.getAttributes().get(AttributeKey.stringKey("foo"))).isEqualTo("bar");
    assertThat(pointData.getAttributes().size()).isEqualTo(1);
    assertThat(pointData.getScale()).isEqualTo(1);
    assertThat(pointData.getZeroCount()).isEqualTo(10);
    assertThat(pointData.hasMin()).isTrue();
    assertThat(pointData.getMin()).isEqualTo(100);
    assertThat(pointData.hasMax()).isTrue();
    assertThat(pointData.getMax()).isEqualTo(1000);
    assertThat(pointData.getPositiveBuckets().getTotalCount()).isEqualTo(3);
    assertThat(pointData.getNegativeBuckets().getTotalCount()).isEqualTo(30);
    assertThat(pointData.getPositiveBuckets().getBucketCounts()).containsExactly(1L, 2L, 3L);
    assertThat(pointData.getNegativeBuckets().getBucketCounts()).containsExactly(50L, 60L, 70L);
    assertThat(pointData.getStartEpochNanos()).isEqualTo(10);
    assertThat(pointData.getEpochNanos()).isEqualTo(20);
    assertThat(pointData.getExemplars()).isEmpty();

    assertThat(pointData.toString())
        .isEqualTo(
            "MutableExponentialHistogramPointData{startEpochNanos=10, epochNanos=20, "
                + "attributes={foo=\"bar\"}, scale=1, sum=2.0, count=43, zeroCount=10, hasMin=true, "
                + "min=100.0, hasMax=true, max=1000.0, "
                + "positiveBuckets=MutableExponentialHistogramBuckets{scale=1, offset=2, "
                + "bucketCounts=[1, 2, 3], totalCount=3}, "
                + "negativeBuckets=MutableExponentialHistogramBuckets{scale=10, offset=20, "
                + "bucketCounts=[50, 60, 70], totalCount=30}, exemplars=[]}");

    MutableExponentialHistogramPointData samePointData = new MutableExponentialHistogramPointData();
    samePointData.set(
        /* scale= */ 1,
        /* sum= */ 2,
        /* zeroCount= */ 10,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        positiveBuckets,
        negativeBuckets,
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        Collections.emptyList());
    assertThat(samePointData).isEqualTo(pointData);
    assertThat(samePointData.hashCode()).isEqualTo(pointData.hashCode());

    MutableExponentialHistogramPointData differentPointData =
        new MutableExponentialHistogramPointData();
    differentPointData.set(
        /* scale= */ 1,
        /* sum= */ 2,
        /* zeroCount= */ 10000000,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        positiveBuckets,
        negativeBuckets,
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        Collections.emptyList());

    assertThat(differentPointData).isNotEqualTo(pointData);
    assertThat(differentPointData.hashCode()).isNotEqualTo(pointData.hashCode());
  }
}
