/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** A gauge instrument that synchronously records {@code long} values. */
@ThreadSafe
public interface LongGaugeOp {

  /**
   * Set the gauge value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The current gauge value.
   */
  void set(long value);
}
