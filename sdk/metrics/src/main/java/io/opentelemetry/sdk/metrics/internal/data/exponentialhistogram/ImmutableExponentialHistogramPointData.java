/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Simple auto value implementation of {@link ExponentialHistogramPointData}. For detailed javadoc
 * on the type, see {@link ExponentialHistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
abstract class ImmutableExponentialHistogramPointData implements ExponentialHistogramPointData {

  ImmutableExponentialHistogramPointData() {}

  /**
   * Create a DoubleExponentialHistogramPointData.
   *
   * @return a DoubleExponentialHistogramPointData.
   */
  static ImmutableExponentialHistogramPointData create(
      int scale,
      double sum,
      long zeroCount,
      @Nullable Double min,
      @Nullable Double max,
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
        min != null,
        min != null ? min : -1,
        max != null,
        max != null ? max : -1,
        positiveBuckets,
        negativeBuckets,
        exemplars);
  }
}
