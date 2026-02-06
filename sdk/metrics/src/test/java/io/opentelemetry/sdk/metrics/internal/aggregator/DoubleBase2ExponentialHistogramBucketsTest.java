/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * These are extra test cases for buckets. Much of this class is already tested via more complex
 * test cases at {@link DoubleBase2ExponentialHistogramAggregatorTest}.
 */
class DoubleBase2ExponentialHistogramBucketsTest {

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_Valid(MemoryMode memoryMode) {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);
    b.record(1);
    b.record(1);
    b.record(1);
    assertThat(b.getTotalCount()).isEqualTo(3);
    assertThat(b.getBucketCounts()).isEqualTo(Collections.singletonList(3L));
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void record_Zero_Throws(MemoryMode memoryMode) {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);
    assertThatThrownBy(() -> b.record(0)).isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void downscale_Valid(MemoryMode memoryMode) {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);
    b.downscale(20); // scale of zero is easy to reason with without a calculator
    b.record(1);
    b.record(2);
    b.record(4);
    assertThat(b.getScale()).isEqualTo(0);
    assertThat(b.getTotalCount()).isEqualTo(3);
    assertThat(b.getBucketCounts()).isEqualTo(Arrays.asList(1L, 1L, 1L));
    assertThat(b.getOffset()).isEqualTo(-1);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void downscale_NegativeIncrement_Throws(MemoryMode memoryMode) {
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);
    assertThatThrownBy(() -> b.downscale(-1)).isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void equalsAndHashCode(MemoryMode memoryMode) {
    DoubleBase2ExponentialHistogramBuckets a = newBuckets(memoryMode);
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);

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
    DoubleBase2ExponentialHistogramBuckets empty = newBuckets(memoryMode);
    empty.downscale(20);
    DoubleBase2ExponentialHistogramBuckets c = newBuckets(memoryMode);
    c.downscale(20);
    assertThat(c.record(1)).isTrue();
    // Record can fail if scale is not set correctly.
    assertThat(c.record(3)).isTrue();
    assertThat(c.getTotalCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void toString_Valid(MemoryMode memoryMode) {
    // Note this test may break once difference implementations for counts are developed since
    // the counts may have different toStrings().
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(memoryMode);
    b.record(1);
    assertThat(b.toString())
        .isEqualTo("DoubleExponentialHistogramBuckets{scale: 20, offset: -1, counts: {-1=1} }");
  }

  @Test
  void testGetBucketCountsWithReusableList() {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(MemoryMode.REUSABLE_DATA);
    b.record(1);
    b.record(1);
    b.record(1);
    assertThat(b.getBucketCounts()).isEqualTo(Collections.singletonList(3L));

    DynamicPrimitiveLongList bucketCounts = DynamicPrimitiveLongList.empty();
    b.getBucketCountsIntoReusableList(bucketCounts);
    assertThat(bucketCounts).isEqualTo(Collections.singletonList(3L));
  }

  @Test
  public void testGetBucketCountsWithReusableListWithEmptyCounts() {
    // Can only effectively test recording of one value here due to downscaling required.
    // More complex recording/downscaling operations are tested in the aggregator.
    DoubleBase2ExponentialHistogramBuckets b = newBuckets(MemoryMode.REUSABLE_DATA);
    assertThat(b.getBucketCounts()).isEmpty();

    DynamicPrimitiveLongList bucketCounts = DynamicPrimitiveLongList.empty();
    b.getBucketCountsIntoReusableList(bucketCounts);
    assertThat(bucketCounts).isEmpty();
  }

  @Test
  public void testDownScaleReusableCountIsOkWhenUsedForSecondTime() {
    DoubleBase2ExponentialHistogramBuckets immutableDataBasedBuckets =
        newBuckets(MemoryMode.IMMUTABLE_DATA);
    immutableDataBasedBuckets.record(0.5);
    immutableDataBasedBuckets.record(1);
    immutableDataBasedBuckets.record(10);
    immutableDataBasedBuckets.downscale(
        2); // scale of zero is easy to reason with without a calculator

    DoubleBase2ExponentialHistogramBuckets reusableDataBasedBuckets =
        newBuckets(MemoryMode.REUSABLE_DATA);
    reusableDataBasedBuckets.record(0.5);
    reusableDataBasedBuckets.record(1);
    reusableDataBasedBuckets.record(10);
    reusableDataBasedBuckets.downscale(
        2); // scale of zero is easy to reason with without a calculator

    assertThat(immutableDataBasedBuckets.getScale()).isEqualTo(reusableDataBasedBuckets.getScale());
    assertThat(immutableDataBasedBuckets.getTotalCount())
        .isEqualTo(reusableDataBasedBuckets.getTotalCount());
    assertThat(immutableDataBasedBuckets.getBucketCounts())
        .isEqualTo(reusableDataBasedBuckets.getBucketCounts());
    assertThat(immutableDataBasedBuckets.getOffset())
        .isEqualTo(reusableDataBasedBuckets.getOffset());

    immutableDataBasedBuckets.downscale(
        3); // scale of zero is easy to reason with without a calculator
    reusableDataBasedBuckets.downscale(
        3); // scale of zero is easy to reason with without a calculator

    assertThat(immutableDataBasedBuckets.getScale()).isEqualTo(reusableDataBasedBuckets.getScale());
    assertThat(immutableDataBasedBuckets.getTotalCount())
        .isEqualTo(reusableDataBasedBuckets.getTotalCount());
    assertThat(immutableDataBasedBuckets.getBucketCounts())
        .isEqualTo(reusableDataBasedBuckets.getBucketCounts());
    assertThat(immutableDataBasedBuckets.getOffset())
        .isEqualTo(reusableDataBasedBuckets.getOffset());
  }

  private static DoubleBase2ExponentialHistogramBuckets newBuckets(MemoryMode memoryMode) {
    return new DoubleBase2ExponentialHistogramBuckets(20, 160, memoryMode);
  }
}
