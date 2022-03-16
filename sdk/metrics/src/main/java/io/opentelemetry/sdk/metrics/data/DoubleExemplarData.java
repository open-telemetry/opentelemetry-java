/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/** An {@link ExemplarData} with {@code double} measurements. */
public interface DoubleExemplarData extends ExemplarData {
  /** Numerical value of the measurement that was recorded. */
  double getValue();
}
