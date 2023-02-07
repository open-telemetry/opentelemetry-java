/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * These are extra test cases for buckets. Much of this class is already tested via more complex
 * test cases at {@link DoubleBase2ExponentialHistogramAggregatorTest}.
 */
class DoubleBase2ExponentialHistogramBucketsTest {

  @Test
  void record_Valid() {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();
    b.record(1);
    b.record(1);
    b.record(1);
    assertThat(b.getTotalCount()).isEqualTo(3);
    assertThat(b.getBucketCounts()).isEqualTo(Collections.singletonList(3L));
  }

  @Test
  void record_Zero_Throws() {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();
    assertThatThrownBy(() -> b.record(0)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void downscale_Valid() {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();
    b.downscale(20); // scale of zero is easy to reason with without a calculator
    b.record(1);
    b.record(2);
    b.record(4);
    assertThat(b.getScale()).isEqualTo(0);
    assertThat(b.getTotalCount()).isEqualTo(3);
    assertThat(b.getBucketCounts()).isEqualTo(Arrays.asList(1L, 1L, 1L));
    assertThat(b.getOffset()).isEqualTo(-1);
  }

  @Test
  void downscale_NegativeIncrement_Throws() {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();
    assertThatThrownBy(() -> b.downscale(-1)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void equalsAndHashCode() {
    DoubleBase2ExponentialHistogramBuckets a = newBuckets();
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();

    assertThat(a).isNotNull();
    assertThat(b).isEqualTo(a);
    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);

    a.record(1);
    assertThat(a).isNotEqualTo(b);
    assertThat(b).isNotEqualTo(a);
    assertThat(a).doesNotHaveSameHashCodeAs(b);

    b.record(1);
    assertThat(b).isEqualTo(a);
    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);

    // Now we start to play with altering offset, but having same effective counts.
    DoubleBase2ExponentialHistogramBuckets empty = newBuckets();
    empty.downscale(20);
    DoubleBase2ExponentialHistogramBuckets c = newBuckets();
    c.downscale(20);
    assertThat(c.record(1)).isTrue();
    // Record can fail if scale is not set correctly.
    assertThat(c.record(3)).isTrue();
    assertThat(c.getTotalCount()).isEqualTo(2);
  }

  @Test
  void toString_Valid() {
    // Note this test may break once difference implementations for counts are developed since
    // the counts may have different toStrings().
    DoubleBase2ExponentialHistogramBuckets b = newBuckets();
    b.record(1);
    assertThat(b.toString())
        .isEqualTo("DoubleExponentialHistogramBuckets{scale: 20, offset: -1, counts: {-1=1} }");
  }

  private static DoubleBase2ExponentialHistogramBuckets newBuckets() {
    return new DoubleBase2ExponentialHistogramBuckets(20, 160);
  }
}
