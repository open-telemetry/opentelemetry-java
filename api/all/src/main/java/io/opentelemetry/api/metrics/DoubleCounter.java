/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A Counter instrument that records {@code double} values.
 *
 * @since 1.10.0
 */
@ThreadSafe
public interface DoubleCounter {
  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   */
  void add(double value);

  /**
   * Records a value with a set of attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   */
  void add(double value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void add(double value, Attributes attributes, Context context);
}
