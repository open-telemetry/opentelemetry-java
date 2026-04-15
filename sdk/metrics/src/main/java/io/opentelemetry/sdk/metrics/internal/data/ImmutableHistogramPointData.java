/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import static io.opentelemetry.sdk.metrics.internal.data.HistogramPointDataValidations.validateFiniteBoundaries;
import static io.opentelemetry.sdk.metrics.internal.data.HistogramPointDataValidations.validateIsStrictlyIncreasing;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * An approximate representation of the distribution of measurements.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableHistogramPointData implements HistogramPointData {

  /**
   * Creates a HistogramPointData. For a Histogram with N defined boundaries, there should be N+1
   * counts.
   *
   * @return a HistogramPointData.
   * @throws IllegalArgumentException if the given boundaries/counts were invalid
   */
  @SuppressWarnings("TooManyParameters")
  public static ImmutableHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double sum,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      List<Double> boundaries,
      List<Long> counts) {
    return create(
        startEpochNanos,
        epochNanos,
        attributes,
        sum,
        hasMin,
        min,
        hasMax,
        max,
        boundaries,
        counts,
        Collections.emptyList());
  }

  /**
   * Creates a HistogramPointData. For a Histogram with N defined boundaries, there should be N+1
   * counts.
   *
   * @return a HistogramPointData.
   * @throws IllegalArgumentException if the given boundaries/counts were invalid
   */
  @SuppressWarnings("TooManyParameters")
  public static ImmutableHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double sum,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      List<Double> boundaries,
      List<Long> counts,
      List<DoubleExemplarData> exemplars) {
    if (counts.size() != boundaries.size() + 1) {
      throw new IllegalArgumentException(
          "invalid counts: size should be "
              + (boundaries.size() + 1)
              + " instead of "
              + counts.size());
    }
    validateIsStrictlyIncreasing(boundaries);
    validateFiniteBoundaries(boundaries);

    long totalCount = 0;
    for (long c : PrimitiveLongList.toArray(counts)) {
      totalCount += c;
    }
    return new AutoValue_ImmutableHistogramPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        sum,
        totalCount,
        hasMin,
        min,
        hasMax,
        max,
        Collections.unmodifiableList(new ArrayList<>(boundaries)),
        Collections.unmodifiableList(new ArrayList<>(counts)),
        exemplars);
  }

  ImmutableHistogramPointData() {}
}
