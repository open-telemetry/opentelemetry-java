/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * These are extra test cases for buckets. Much of this class is already tested via more complex
 * test cases at {@link DoubleExponentialHistogramAggregatorTest}.
 */
public class DoubleExponentialHistogramBucketsTest {

  @Test
  void testRecordSimple() {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();
    b.record(1);
    b.record(1);
    b.record(1);
    assertThat(b).hasTotalCount(3).hasCounts(Collections.singletonList(3L));
  }

  @Test
  void testRecordShouldError() {
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();
    assertThatThrownBy(() -> b.record(0)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testDownscale() {
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();
    b.downscale(20); // scale of zero is easy to reason with without a calculator
    b.record(1);
    b.record(2);
    b.record(4);
    assertThat(b.getScale()).isEqualTo(0);
    assertThat(b).hasTotalCount(3).hasCounts(Arrays.asList(1L, 1L, 1L)).hasOffset(0);
  }

  @Test
  void testDownscaleShouldError() {
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();
    assertThatThrownBy(() -> b.downscale(-1)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testEqualsAndHashCode() {
    DoubleExponentialHistogramBuckets a = new DoubleExponentialHistogramBuckets();
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();

    assertNotEquals(a, null);
    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());

    a.record(1);
    assertNotEquals(a, b);
    assertNotEquals(b, a);
    assertNotEquals(a.hashCode(), b.hashCode());

    b.record(1);
    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  void testToString() {
    // Note this test may break once difference implementations for counts are developed since
    // the counts may have different toStrings().
    DoubleExponentialHistogramBuckets b = new DoubleExponentialHistogramBuckets();
    b.record(1);
    assertThat(b.toString())
        .isEqualTo("DoubleExponentialHistogramBuckets{scale: 20, offset: 0, counts: {0=1} }");
  }
}
