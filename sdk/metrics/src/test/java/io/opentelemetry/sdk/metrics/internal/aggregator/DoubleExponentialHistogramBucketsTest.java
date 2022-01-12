/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * These are extra test cases for buckets. Much of this class is already tested via more complex
 * test cases at {@link DoubleExponentialHistogramAggregatorTest}.
 */
public class DoubleExponentialHistogramBucketsTest {

  static Stream<ExponentialBucketStrategy> bucketStrategies() {
    return Stream.of(
        ExponentialBucketStrategy.newStrategy(20, 320, ExponentialCounterFactory.mapCounter()),
        ExponentialBucketStrategy.newStrategy(
            20, 320, ExponentialCounterFactory.circularBufferCounter()));
  }

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testRecordSimple(ExponentialBucketStrategy buckets) {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();
    b.record(1);
    b.record(1);
    b.record(1);
    assertThat(b).hasTotalCount(3).hasCounts(Collections.singletonList(3L));
  }

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testRecordShouldError(ExponentialBucketStrategy buckets) {
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();
    assertThatThrownBy(() -> b.record(0)).isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testDownscale(ExponentialBucketStrategy buckets) {
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();
    b.downscale(20); // scale of zero is easy to reason with without a calculator
    b.record(1);
    b.record(2);
    b.record(4);
    assertThat(b.getScale()).isEqualTo(0);
    assertThat(b).hasTotalCount(3).hasCounts(Arrays.asList(1L, 1L, 1L)).hasOffset(0);
  }

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testDownscaleShouldError(ExponentialBucketStrategy buckets) {
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();
    assertThatThrownBy(() -> b.downscale(-1)).isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testEqualsAndHashCode(ExponentialBucketStrategy buckets) {
    DoubleExponentialHistogramBuckets a = buckets.newBuckets();
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();

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

  @ParameterizedTest
  @MethodSource("bucketStrategies")
  void testToString(ExponentialBucketStrategy buckets) {
    // Note this test may break once difference implementations for counts are developed since
    // the counts may have different toStrings().
    DoubleExponentialHistogramBuckets b = buckets.newBuckets();
    b.record(1);
    assertThat(b.toString())
        .isEqualTo("DoubleExponentialHistogramBuckets{scale: 20, offset: 0, counts: {0=1} }");
  }
}
