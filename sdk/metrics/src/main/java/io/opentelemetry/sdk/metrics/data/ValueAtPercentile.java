/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/** A value within a summary. */
public interface ValueAtPercentile {
  /**
   * The percentile of a distribution. Must be in the interval [0.0, 100.0].
   *
   * @return the percentile.
   */
  double getPercentile();

  /**
   * The value at the given percentile of a distribution.
   *
   * @return the value at the percentile.
   */
  double getValue();
}
