/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import java.util.List;

/** A Reservoir sampler that preserves the latest seen measurement per-histogram bucket. */
public class HistogramBucketExemplarReservoir extends AbstractFixedSizeExemplarReservoir {
  private final double[] boundaries;

  /** Constructs a new histogram bucket exemplar reservoir using standard configuration. */
  public static HistogramBucketExemplarReservoir create(Clock clock, List<Double> boundaries) {
    return new HistogramBucketExemplarReservoir(
        clock, ExplicitBucketHistogramUtils.createBoundaryArray(boundaries));
  }

  /**
   * Constructs a new reservoir sampler that aligns exemplars with histogram buckets.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param boundaries Histogram bucket boundaries.
   */
  HistogramBucketExemplarReservoir(Clock clock, double[] boundaries) {
    super(clock, boundaries.length + 1);
    this.boundaries = boundaries;
  }

  @Override
  protected int bucketFor(double value, Attributes attributes, Context context) {
    return ExplicitBucketHistogramUtils.findBucketIndex(boundaries, value);
  }
}
