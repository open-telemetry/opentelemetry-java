/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A single data point that summarizes the values in a time series of numeric values.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time
 */
@Immutable
@AutoValue
public abstract class ImmutableSummaryPointData implements SummaryPointData {
  /**
   * Creates a {@link SummaryPointData}.
   *
   * @param startEpochNanos (optional) The starting time for the period where this point was
   *     sampled.
   * @param epochNanos The ending time for the period when this value was sampled.
   * @param attributes The set of attributes associated with this point.
   * @param count The number of measurements being sumarized.
   * @param sum The sum of measuremnts being sumarized.
   * @param percentileValues Calculations of percentile values from measurements.
   */
  public static ImmutableSummaryPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long count,
      double sum,
      List<ValueAtQuantile> percentileValues) {
    return new AutoValue_ImmutableSummaryPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        Collections.emptyList(),
        count,
        sum,
        percentileValues);
  }

  ImmutableSummaryPointData() {}
}
