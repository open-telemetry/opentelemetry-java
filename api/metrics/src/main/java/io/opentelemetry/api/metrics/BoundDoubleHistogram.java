/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/** A histogram instrument that records {@code long} values with pre-associated attributes. */
@ThreadSafe
public interface BoundDoubleHistogram {
  /**
   * Records a value with a pre-bound set of attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The amount of the measurement.
   */
  void record(double value);

  /**
   * Records a value with a pre-bound set of attributes.
   *
   * @param value The amount of the measurement.
   * @param context The explicit context to associate with this measurement.
   */
  void record(double value, Context context);

  /**
   * Unbinds the current bound instance from the {@link DoubleHistogram}.
   *
   * <p>After this method returns the current instance is considered invalid (not being managed by
   * the instrument).
   */
  void unbind();
}
