/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A Counter instrument that records {@code long} values.
 *
 * @since 1.10.0
 */
@ThreadSafe
public interface LongCounter {

  /**
   * Returns {@code true} if the counter is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #add(long)}, {@link #add(long, Attributes)}, or {@link #add(long, Attributes,
   * Context)}.
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
   * @param value The increment amount. MUST be non-negative.
   */
  void add(long value);

  /**
   * Records a value with a set of attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   */
  void add(long value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void add(long value, Attributes attributes, Context context);

  /**
   * Remove the instrument.
   *
   * @param attributes A set of attributes to identify the instrument.
   * @since 1.56.0
   */
  default void remove(Attributes attributes) {
    remove(attributes, Context.current());
  }

  /**
   * Remove the instrument.
   *
   * @param attributes A set of attributes to identify the instrument.
   * @param context The explicit context to associate with this measurement.
   * @since 1.56.0
   */
  default void remove(Attributes attributes, Context context) {}
}
