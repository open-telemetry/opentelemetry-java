/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * An interface for an exemplar reservoir of samples.
 *
 * <p>This represents a reservoir for a specific "point" of metric data.
 */
public interface ExemplarReservoir {

  /** An exemplar reservoir that stores no exemplars. */
  static ExemplarReservoir noSamples() {
    return NoExemplarReservoir.INSTANCE;
  }

  /** Wraps a {@link ExemplarReservoir} with a measurement pre-filter. */
  static ExemplarReservoir filtered(ExemplarFilter filter, ExemplarReservoir original) {
    // Optimisation on memory usage.
    if (filter == ExemplarFilter.neverSample()) {
      return ExemplarReservoir.noSamples();
    }
    return new FilteredExemplarReservoir(filter, original);
  }

  /**
   * A Reservoir sampler with fixed size that stores the given number of exemplars.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The maximum number of exemplars to preserve.
   * @param randomSupplier The random number generater to use for sampling.
   */
  static ExemplarReservoir fixedSizeReservoir(
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
  static ExemplarReservoir histogramBucketReservoir(Clock clock, List<Double> boundaries) {
    return HistogramBucketExemplarReservoir.create(clock, boundaries);
  }

  /** Offers a {@code long} measurement to be sampled. */
  void offerMeasurement(long value, Attributes attributes, Context context);

  /** Offers a {@code double} measurement to be sampled. */
  void offerMeasurement(double value, Attributes attributes, Context context);

  /**
   * Builds (an immutable) list of Exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     ExemplarData}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<ExemplarData> collectAndReset(Attributes pointAttributes);
}
