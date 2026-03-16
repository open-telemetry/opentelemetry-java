/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A gauge instrument that synchronously records {@code long} values.
 *
 * @since 1.38.0
 */
@ThreadSafe
public interface LongGauge {

  /**
   * Returns {@code true} if the gauge is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #set(long)}, {@link #set(long, Attributes)}, or {@link #set(long, Attributes,
   * Context)}.
   *
   * @since 1.61.0
   */
  default boolean isEnabled() {
    return true;
  }

  /**
   * Set the gauge value.
   *
   * @param value The current gauge value.
   */
  void set(long value);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The current gauge value.
   * @param attributes A set of attributes to associate with the value.
   */
  void set(long value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The current gauge value.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void set(long value, Attributes attributes, Context context);

  /**
   * Finish the instrument record.
   *
   * @param attributes A set of attributes to identify the instrument.
   * @since 1.56.0
   */
  default void finish(Attributes attributes) {
    finish(attributes, Context.current());
  }

  /**
   * Finish the instrument record.
   *
   * @param attributes A set of attributes to identify the instrument.
   * @param context The explicit context to associate with this measurement.
   * @since 1.56.0
   */
  default void finish(Attributes attributes, Context context) {}
}
