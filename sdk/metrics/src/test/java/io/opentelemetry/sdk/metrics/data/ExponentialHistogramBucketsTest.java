/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ExponentialHistogramBucketsTest {

  @Test
  void create() {
    ExponentialHistogramBuckets buckets =
        ExponentialHistogramBuckets.create(1, 2, Arrays.asList(3L, 4L));
    assertThat(buckets.getBucketCounts()).containsExactly(3L, 4L);
    assertThat(buckets.getScale()).isEqualTo(1);
    assertThat(buckets.getOffset()).isEqualTo(2);
  }
}
