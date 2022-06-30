/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * A quantile value within a {@link SummaryPointData}.
 *
 * @since 1.14.0
 */
@Immutable
public interface ValueAtQuantile {
  /** Returns the quantile of a distribution. Must be in the interval [0.0, 1.0]. */
  double getQuantile();

  /** Returns the value at the given quantile of a distribution. */
  double getValue();
}
