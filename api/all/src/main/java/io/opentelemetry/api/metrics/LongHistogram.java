/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A Histogram instrument that records {@code long} values.
 *
 * @since 1.10.0
 */
@ThreadSafe
public interface LongHistogram {

  /**
   * Records a value.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   */
  void record(long value);

  /**
   * Records a value with a set of attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   */
  void record(long value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The amount of the measurement. MUST be non-negative.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void record(long value, Attributes attributes, Context context);
}
