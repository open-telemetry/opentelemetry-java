/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ExponentialHistogramIndexer {

  private static final Map<Integer, ExponentialHistogramIndexer> cache = new ConcurrentHashMap<>();

  /** Bit mask used to isolate exponent of IEEE 754 double precision number. */
  private static final long EXPONENT_BIT_MASK = 0x7FF0000000000000L;

  /** Bit mask used to isolate the significand of IEEE 754 double precision number. */
  private static final long SIGNIFICAND_BIT_MASK = 0xFFFFFFFFFFFFFL;

  /** Bias used in representing the exponent of IEEE 554 double precision number. */
  private static final int EXPONENT_BIAS = 1023;

  /**
   * The number of bits used to represent the significand of IEEE 754 double precision number,
   * excluding the implicit bit.
   */
  private static final int SIGNIFICAND_WIDTH = 52;

  private static final double LOG_BASE2_E = 1D / Math.log(2);

  private final int scale;
  private final double scaleFactor;

  private ExponentialHistogramIndexer(int scale) {
    this.scale = scale;
    this.scaleFactor = computeScaleFactor(scale);
  }

  /** Get an indexer for the given scale. Indexers are cached and reused for */
  public static ExponentialHistogramIndexer get(int scale) {
    return cache.computeIfAbsent(scale, unused -> new ExponentialHistogramIndexer(scale));
  }

  /**
   * Compute the index for the given value.
   *
   * <p>The algorithm to retrieve the index is specified in the <a
   * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponential-buckets">OpenTelemetry
   * specification</a>.
   *
   * @param value Measured value (must be non-zero).
   * @return the index of the bucket which the value maps to.
   */
  int computeIndex(double value) {
    double absValue = Math.abs(value);
    if (scale > 0) {
      return getIndexByLogarithm(absValue);
    }
    if (scale == 0) {
      return mapToIndexScaleZero(absValue);
    }
    // scale < 0
    return mapToIndexScaleZero(absValue) >> -scale;
  }

  private int getIndexByLogarithm(double value) {
    return (int) Math.ceil(Math.log(value) * scaleFactor) - 1;
  }

  private static int mapToIndexScaleZero(double value) {
    long rawBits = Double.doubleToLongBits(value);
    long rawExponent = (rawBits & EXPONENT_BIT_MASK) >> SIGNIFICAND_WIDTH;
    long rawSignificand = rawBits & SIGNIFICAND_BIT_MASK;
    int ieeeExponent = (int) (rawExponent - EXPONENT_BIAS);
    if (rawSignificand == 0) {
      return ieeeExponent - 1;
    }
    return ieeeExponent;
  }

  private static double computeScaleFactor(int scale) {
    return Math.scalb(LOG_BASE2_E, scale);
  }
}
