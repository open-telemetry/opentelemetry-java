/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import org.junit.jupiter.api.Test;

class MutableExponentialHistogramBucketsTest {

  @Test
  void testSanity() {
    MutableExponentialHistogramBuckets buckets = new MutableExponentialHistogramBuckets();
    assertThat(buckets.getScale()).isEqualTo(0);
    assertThat(buckets.getOffset()).isEqualTo(0);
    assertThat(buckets.getTotalCount()).isEqualTo(0);
    assertThat(buckets.getBucketCounts()).isEmpty();
    assertThat(buckets.getReusableBucketCountsList()).isEmpty();

    DynamicPrimitiveLongList bucketCounts = DynamicPrimitiveLongList.of(1, 2, 3);
    buckets.set(1, 2, 3, bucketCounts);

    assertThat(buckets.getScale()).isEqualTo(1);
    assertThat(buckets.getOffset()).isEqualTo(2);
    assertThat(buckets.getTotalCount()).isEqualTo(3);
    assertThat(buckets.getBucketCounts()).containsExactly(1L, 2L, 3L);
    assertThat(buckets.getReusableBucketCountsList()).containsExactly(1L, 2L, 3L);

    assertThat(buckets.toString())
        .isEqualTo(
            "MutableExponentialHistogramBuckets{scale=1, offset=2, bucketCounts=[1, 2, 3], totalCount=3}");

    MutableExponentialHistogramBuckets sameBuckets = new MutableExponentialHistogramBuckets();
    sameBuckets.set(1, 2, 3, DynamicPrimitiveLongList.of(1, 2, 3));
    assertThat(sameBuckets).isEqualTo(buckets);
    assertThat(sameBuckets.hashCode()).isEqualTo(buckets.hashCode());

    sameBuckets.set(1, 2, 3, DynamicPrimitiveLongList.of(1, 20, 3));
    assertThat(sameBuckets).isNotEqualTo(buckets);
    assertThat(sameBuckets.hashCode()).isNotEqualTo(buckets.hashCode());
  }
}
