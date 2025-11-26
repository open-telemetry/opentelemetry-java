/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import javax.annotation.concurrent.Immutable;

/**
 * A quantile value within a {@link SummaryPointData}.
 *
 * @since 1.14.0
 */
@Immutable
public interface ValueAtQuantile {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static ValueAtQuantile create(double quantile, double value) {
    return ImmutableValueAtQuantile.create(quantile, value);
  }

  /** Returns the quantile of a distribution. Must be in the interval [0.0, 1.0]. */
  double getQuantile();

  /** Returns the value at the given quantile of a distribution. */
  double getValue();
}
