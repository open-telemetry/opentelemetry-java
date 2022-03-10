/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/** A value within a summary. */
public interface ValueAtQuantile {
  /** Returns the quantile of a distribution. Must be in the interval [0.0, 1.0]. */
  double getQuantile();

  /** Returns the value at the given percentile of a distribution. */
  double getValue();
}
