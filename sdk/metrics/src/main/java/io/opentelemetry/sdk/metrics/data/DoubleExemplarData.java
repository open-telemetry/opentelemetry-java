/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * Exemplar data for {@code double} measurements.
 *
 * @since 1.14.0
 */
@Immutable
public interface DoubleExemplarData extends ExemplarData {
  /** Numerical value of the measurement that was recorded. */
  double getValue();
}
