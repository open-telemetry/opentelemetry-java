/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ExponentialHistogramBuckets}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class ImmutableExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  ImmutableExponentialHistogramBuckets() {}

  /**
   * Create a ExponentialHistogramBuckets.
   *
   * @return a ExponentialHistogramBuckets.
   */
  @SuppressWarnings("TooManyParameters")
  public static ExponentialHistogramBuckets create(int scale, int offset, List<Long> bucketCounts) {
    return new AutoValue_ImmutableExponentialHistogramBuckets(
        scale, offset, bucketCounts, bucketCounts.stream().mapToLong(l -> l).sum());
  }
}
