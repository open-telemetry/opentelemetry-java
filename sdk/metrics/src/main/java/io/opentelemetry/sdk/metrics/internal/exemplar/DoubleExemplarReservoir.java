/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * An interface for an exemplar reservoir of samples.
 *
 * <p>This represents a reservoir for a specific "point" of metric data.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface DoubleExemplarReservoir extends ExemplarReservoir<DoubleExemplarData> {

  /** Wraps a {@link DoubleExemplarReservoir} with a measurement pre-filter. */
  static DoubleExemplarReservoir filtered(ExemplarFilter filter, DoubleExemplarReservoir original) {
    // Optimisation on memory usage.
    if (filter == ExemplarFilter.neverSample()) {
      return NoopDoubleExemplarReservoir.INSTANCE;
    }
    return new FilteredDoubleExemplarReservoir(filter, original);
  }

  /**
   * A Reservoir sampler with fixed size that stores the given number of exemplars.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The maximum number of exemplars to preserve.
   * @param randomSupplier The random number generater to use for sampling.
   */
  static DoubleExemplarReservoir fixedSizeReservoir(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return new FixedSizeExemplarReservoir(clock, size, randomSupplier);
  }

  /**
   * A Reservoir sampler that preserves the latest seen measurement per-histogram bucket.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param boundaries A list of (inclusive) upper bounds for the histogram. Should be in order from
   *     lowest to highest.
   */
  static DoubleExemplarReservoir histogramBucketReservoir(Clock clock, List<Double> boundaries) {
    return HistogramBucketExemplarReservoir.create(clock, boundaries);
  }

  /** Offers a {@code double} measurement to be sampled. */
  void offerMeasurement(double value, Attributes attributes, Context context);
}
