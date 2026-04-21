/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An UpDownCounter instrument that records {@code double} values.
 *
 * @since 1.10.0
 */
@ThreadSafe
public interface DoubleUpDownCounter {

  /**
   * Returns {@code true} if the up down counter is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #add(double)}, {@link #add(double, Attributes)}, or {@link #add(double,
   * Attributes, Context)}.
   *
   * @since 1.61.0
   */
  default boolean isEnabled() {
    return true;
  }

  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   */
  void add(double value);

  /**
   * Records a value with a set of attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   * @param attributes A set of attributes to associate with the value.
   */
  void add(double value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The increment amount. May be positive, negative or zero.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void add(double value, Attributes attributes, Context context);

  DoubleUpDownCounterOp bind(Attributes attributes);
}
