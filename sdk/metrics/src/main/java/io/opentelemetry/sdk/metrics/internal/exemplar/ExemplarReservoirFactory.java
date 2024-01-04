/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * An interface for constructing an appropriate ExemplarReservoir for a given metric "memory cell".
 */
public interface ExemplarReservoirFactory {
  ExemplarReservoir<LongExemplarData> createLongExemplarReservoir();

  ExemplarReservoir<DoubleExemplarData> createDoubleExemplarReservoir();

  /** An exemplar reservoir that stores no exemplars. */
  static ExemplarReservoirFactory noSamples() {
    return new ExemplarReservoirFactory() {
      @Override
      public ExemplarReservoir<LongExemplarData> createLongExemplarReservoir() {
        return NoopExemplarReservoir.LONG_INSTANCE;
      }

      @Override
      public ExemplarReservoir<DoubleExemplarData> createDoubleExemplarReservoir() {
        return NoopExemplarReservoir.DOUBLE_INSTANCE;
      }

      @Override
      public String toString() {
        return "noSamples";
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
  static ExemplarReservoirFactory fixedSize(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return new ExemplarReservoirFactory() {
      @Override
      public ExemplarReservoir<LongExemplarData> createLongExemplarReservoir() {
        return RandomFixedSizeExemplarReservoir.createLong(clock, size, randomSupplier);
      }

      @Override
      public ExemplarReservoir<DoubleExemplarData> createDoubleExemplarReservoir() {
        return RandomFixedSizeExemplarReservoir.createDouble(clock, size, randomSupplier);
      }

      @Override
      public String toString() {
        return "fixedSize(" + size + ")";
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
  static ExemplarReservoirFactory histogramBucket(Clock clock, List<Double> boundaries) {
    return new ExemplarReservoirFactory() {
      @Override
      public ExemplarReservoir<LongExemplarData> createLongExemplarReservoir() {
        throw new UnsupportedOperationException(
            "Cannot create long exemplars for histogram buckets");
      }

      @Override
      public ExemplarReservoir<DoubleExemplarData> createDoubleExemplarReservoir() {
        return new HistogramExemplarReservoir(clock, boundaries);
      }

      @Override
      public String toString() {
        return "histogramBucket(" + boundaries + ")";
      }
    };
  }
}
