/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** An UpDownCounter instrument that records {@code long} values. */
@ThreadSafe
public interface LongUpDownCounterOp {

  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   */
  void add(long value);
}
