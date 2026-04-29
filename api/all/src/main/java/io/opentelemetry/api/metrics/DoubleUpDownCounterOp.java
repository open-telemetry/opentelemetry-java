/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** An UpDownCounter instrument that records {@code double} values. */
@ThreadSafe
public interface DoubleUpDownCounterOp {

  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   */
  void add(double value);
}
