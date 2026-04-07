/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** A Histogram instrument that records {@code long} values. */
@ThreadSafe
public interface LongHistogramOp {

  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   */
  void record(long value);
}
