/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/** An {@link ExemplarData} with {@code long} measurements. */
public interface LongExemplarData extends ExemplarData {
  /** Numerical value of the measurement that was recorded. */
  long getValue();
}
