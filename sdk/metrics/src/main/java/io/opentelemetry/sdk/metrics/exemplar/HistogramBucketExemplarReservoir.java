/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import java.util.function.Function;

/** A Reservoir sampler that preserves the latest seen measurement per-histogram bucket. */
public class HistogramBucketExemplarReservoir extends FixedSizeExemplarReservoir {
  private final Function<Double, Integer> findBucketIndex;

  public HistogramBucketExemplarReservoir(
      Clock clock, int bucketCount, Function<Double, Integer> findBucketIndex) {
    super(clock, bucketCount);
    this.findBucketIndex = findBucketIndex;
  }

  @Override
  protected int bucketFor(long value, Attributes attributes, Context context) {
    return bucketFor((double) value, attributes, context);
  }

  @Override
  protected int bucketFor(double value, Attributes attributes, Context context) {
    return findBucketIndex.apply(value);
  }
}
