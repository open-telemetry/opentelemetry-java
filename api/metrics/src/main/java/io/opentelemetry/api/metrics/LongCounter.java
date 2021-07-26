/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/** A counter instrument that records {@code long} values. */
@ThreadSafe
public interface LongCounter extends Counter {

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
   * @param attributes A set of attributes to associate with the count.
   */
  void add(long value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param attributes A set of attributes to associate with the count.
   * @param context The explicit context to associate with this measurement.
   */
  void add(long value, Attributes attributes, Context context);

  /**
   * Constructs a bound version of this instrument where all recorded values use the given
   * attributes.
   */
  BoundLongCounter bind(Attributes attributes);
}
