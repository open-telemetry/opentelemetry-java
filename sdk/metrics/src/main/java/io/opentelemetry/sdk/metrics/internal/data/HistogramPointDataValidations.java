/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.List;

/**
 * Validations for {@link HistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class HistogramPointDataValidations {

  private HistogramPointDataValidations() {}

  static void validateIsStrictlyIncreasing(List<Double> xs) {
    for (int i = 0; i < xs.size() - 1; i++) {
      if (xs.get(i).compareTo(xs.get(i + 1)) >= 0) {
        throw new IllegalArgumentException("invalid boundaries: " + xs);
      }
    }
  }

  static void validateFiniteBoundaries(List<Double> boundaries) {
    if (!boundaries.isEmpty()
        && (boundaries.get(0).isInfinite() || boundaries.get(boundaries.size() - 1).isInfinite())) {
      throw new IllegalArgumentException("invalid boundaries: contains explicit +/-Inf");
    }
  }
}
