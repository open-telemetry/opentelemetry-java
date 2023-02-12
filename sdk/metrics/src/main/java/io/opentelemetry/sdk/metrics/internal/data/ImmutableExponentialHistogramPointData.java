/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ExponentialHistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class ImmutableExponentialHistogramPointData
    implements ExponentialHistogramPointData {

  ImmutableExponentialHistogramPointData() {}

  /**
   * Create a DoubleExponentialHistogramPointData.
   *
   * @return a DoubleExponentialHistogramPointData.
   */
  @SuppressWarnings("TooManyParameters")
  public static ExponentialHistogramPointData create(
      int scale,
      double sum,
      long zeroCount,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      ExponentialHistogramBuckets positiveBuckets,
      ExponentialHistogramBuckets negativeBuckets,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars) {

    long count = zeroCount + positiveBuckets.getTotalCount() + negativeBuckets.getTotalCount();

    return new AutoValue_ImmutableExponentialHistogramPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        scale,
        sum,
        count,
        zeroCount,
        hasMin,
        min,
        hasMax,
        max,
        positiveBuckets,
        negativeBuckets,
        exemplars);
  }
}
