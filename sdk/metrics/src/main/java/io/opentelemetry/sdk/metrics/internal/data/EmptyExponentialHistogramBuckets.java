/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An empty {@link ExponentialHistogramBuckets}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
public abstract class EmptyExponentialHistogramBuckets implements ExponentialHistogramBuckets {

  private static final Map<Integer, ExponentialHistogramBuckets> ZERO_BUCKETS =
      new ConcurrentHashMap<>();

  EmptyExponentialHistogramBuckets() {}

  public static ExponentialHistogramBuckets get(int scale) {
    return ZERO_BUCKETS.computeIfAbsent(
        scale,
        scale1 ->
            new AutoValue_EmptyExponentialHistogramBuckets(scale1, 0, Collections.emptyList(), 0));
  }
}
