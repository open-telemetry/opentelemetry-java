/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/** A counter instrument that records {@code long} values with pre-associated attributes. */
@ThreadSafe
public interface BoundLongCounter {
  /**
   * Records a value with pre-bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   */
  void add(long value);

  /**
   * Records a value with pre-bound attributes.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param context The explicit context to associate with this measurement.
   */
  void add(long value, Context context);

  void unbind();
}
