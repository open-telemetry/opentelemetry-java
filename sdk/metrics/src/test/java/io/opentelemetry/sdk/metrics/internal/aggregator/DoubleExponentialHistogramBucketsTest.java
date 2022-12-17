/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.testing.assertj.MetricAssertions;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * These are extra test cases for buckets. Much of this class is already tested via more complex
 * test cases at {@link DoubleExponentialHistogramAggregatorTest}.
 */
class DoubleExponentialHistogramBucketsTest {

  @Test
  void record_Valid() {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleExponentialHistogramBuckets b = newBuckets();
    b.record(1);
    b.record(1);
    b.record(1);
    MetricAssertions.assertThat(b).hasTotalCount(3).hasCounts(Collections.singletonList(3L));
  }

  @Test
  void record_Zero_Throws() {
    DoubleExponentialHistogramBuckets b = newBuckets();
    assertThatThrownBy(() -> b.record(0)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void downscale_Valid() {
    DoubleExponentialHistogramBuckets b = newBuckets();
    b.downscale(20); // scale of zero is easy to reason with without a calculator
    b.record(1);
    b.record(2);
    b.record(4);
    assertThat(b.getScale()).isEqualTo(0);
    MetricAssertions.assertThat(b)
        .hasTotalCount(3)
        .hasCounts(Arrays.asList(1L, 1L, 1L))
        .hasOffset(-1);
  }

  @Test
  void downscale_NegativeIncrement_Throws() {
    DoubleExponentialHistogramBuckets b = newBuckets();
    assertThatThrownBy(() -> b.downscale(-1)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void equalsAndHashCode() {
    DoubleExponentialHistogramBuckets a = newBuckets();
    DoubleExponentialHistogramBuckets b = newBuckets();

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
    DoubleExponentialHistogramBuckets empty = newBuckets();
    empty.downscale(20);
    DoubleExponentialHistogramBuckets c = newBuckets();
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
    DoubleExponentialHistogramBuckets b = newBuckets();
    b.record(1);
    assertThat(b.toString())
        .isEqualTo("DoubleExponentialHistogramBuckets{scale: 20, offset: -1, counts: {-1=1} }");
  }

  private static DoubleExponentialHistogramBuckets newBuckets() {
    return new DoubleExponentialHistogramBuckets(20, 160);
  }
}
