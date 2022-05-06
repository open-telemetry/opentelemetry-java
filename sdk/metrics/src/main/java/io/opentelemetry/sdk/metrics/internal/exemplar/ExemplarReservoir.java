/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
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
public interface ExemplarReservoir<T extends ExemplarData> {

  /** Wraps a {@link ExemplarReservoir} with a measurement pre-filter. */
  static <T extends ExemplarData> ExemplarReservoir<T> filtered(
      ExemplarFilter filter, ExemplarReservoir<T> original) {
    return new FilteredExemplarReservoir<>(filter, original);
  }

  /** A double exemplar reservoir that stores no exemplars. */
  static ExemplarReservoir<DoubleExemplarData> doubleNoSamples() {
    return NoopExemplarReservoir.DOUBLE_INSTANCE;
  }

  /** A long exemplar reservoir that stores no exemplars. */
  static ExemplarReservoir<LongExemplarData> longNoSamples() {
    return NoopExemplarReservoir.LONG_INSTANCE;
  }

  /**
   * A double reservoir with fixed size that stores the given number of exemplars.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The maximum number of exemplars to preserve.
   * @param randomSupplier The random number generator to use for sampling.
   */
  static ExemplarReservoir<DoubleExemplarData> doubleFixedSizeReservoir(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return RandomFixedSizeExemplarReservoir.createDouble(clock, size, randomSupplier);
  }

  /**
   * A long reservoir with fixed size that stores the given number of exemplars.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The maximum number of exemplars to preserve.
   * @param randomSupplier The random number generator to use for sampling.
   */
  static ExemplarReservoir<LongExemplarData> longFixedSizeReservoir(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return RandomFixedSizeExemplarReservoir.createLong(clock, size, randomSupplier);
  }

  /**
   * A Reservoir sampler that preserves the latest seen measurement per-histogram bucket.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param boundaries A list of (inclusive) upper bounds for the histogram. Should be in order from
   *     lowest to highest.
   */
  static ExemplarReservoir<DoubleExemplarData> histogramBucketReservoir(
      Clock clock, List<Double> boundaries) {
    return new HistogramExemplarReservoir(clock, boundaries);
  }

  /** Offers a {@code double} measurement to be sampled. */
  void offerDoubleMeasurement(double value, Attributes attributes, Context context);

  /** Offers a {@code long} measurement to be sampled. */
  void offerLongMeasurement(long value, Attributes attributes, Context context);

  /**
   * Returns an immutable list of Exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     ExemplarData}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<T> collectAndReset(Attributes pointAttributes);
}
