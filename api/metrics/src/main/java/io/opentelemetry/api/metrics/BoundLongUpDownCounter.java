/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/** An up-down-counter instrument with pre-bound attributes. */
@ThreadSafe
public interface BoundLongUpDownCounter {
  /**
   * Records a value with pre-bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. May be positive, negative or zero.
   */
  void add(long value);

  /**
   * Records a value with a pre-bound attributes.
   *
   * @param value The increment amount. May be positive, negative or zero.
   * @param context The explicit context to associate with this measurement.
   */
  void add(long value, Context context);

  /**
   * Unbinds the current bound instance from the {@link LongUpDownCounter}.
   *
   * <p>After this method returns the current instance is considered invalid (not being managed by
   * the instrument).
   */
  void unbind();
}
