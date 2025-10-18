/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.sdk.common.Clock;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * An exemplar reservoir factory is responsible for creating exemplar reservoir instances (i.e.
 * {@link DoubleExemplarReservoir} or {@link LongExemplarReservoir}) for aggregation handles.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExemplarReservoirFactory {

  /**
   * Wraps exemplar reservoirs returned by a {@link ExemplarReservoirFactory} with a measurement
   * pre-filter.
   */
  static ExemplarReservoirFactory filtered(
      ExemplarFilter filter, ExemplarReservoirFactory original) {
    return new ExemplarReservoirFactory() {
      @Override
      public DoubleExemplarReservoir createDoubleExemplarReservoir() {
        return new DoubleFilteredExemplarReservoir(
            filter, original.createDoubleExemplarReservoir());
      }

      @Override
      public LongExemplarReservoir createLongExemplarReservoir() {
        return new LongFilteredExemplarReservoir(filter, original.createLongExemplarReservoir());
      }
    };
  }

  /** An exemplar reservoir that stores no exemplars. */
  static ExemplarReservoirFactory noSamples() {
    return new ExemplarReservoirFactory() {
      @Override
      public DoubleExemplarReservoir createDoubleExemplarReservoir() {
        return NoopExemplarReservoir.INSTANCE;
      }

      @Override
      public LongExemplarReservoir createLongExemplarReservoir() {
        return NoopExemplarReservoir.INSTANCE;
      }
    };
  }

  /**
   * A reservoir with fixed size that stores the given number of exemplars.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The maximum number of exemplars to preserve.
   * @param randomSupplier The random number generator to use for sampling.
   */
  static ExemplarReservoirFactory fixedSizeReservoir(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return new ExemplarReservoirFactory() {
      @Override
      public DoubleExemplarReservoir createDoubleExemplarReservoir() {
        return RandomFixedSizeExemplarReservoir.create(clock, size, randomSupplier);
      }

      @Override
      public LongExemplarReservoir createLongExemplarReservoir() {
        return RandomFixedSizeExemplarReservoir.create(clock, size, randomSupplier);
      }
    };
  }

  /**
   * A Reservoir sampler that preserves the latest seen measurement per-histogram bucket.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param boundaries A list of (inclusive) upper bounds for the histogram. Should be in order from
   *     lowest to highest.
   */
  static ExemplarReservoirFactory histogramBucketReservoir(Clock clock, List<Double> boundaries) {
    return new ExemplarReservoirFactory() {
      @Override
      public DoubleExemplarReservoir createDoubleExemplarReservoir() {
        return new HistogramExemplarReservoir(clock, boundaries);
      }

      @Override
      public LongExemplarReservoir createLongExemplarReservoir() {
        return new HistogramExemplarReservoir(clock, boundaries);
      }
    };
  }

  /** Create an exemplar reservoir for double measurements. */
  DoubleExemplarReservoir createDoubleExemplarReservoir();

  /** Create an exemplar reservoir for long measurements. */
  LongExemplarReservoir createLongExemplarReservoir();
}
