/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A gauge instrument that synchronously records {@code double} values.
 *
 * @since 1.38.0
 */
@ThreadSafe
public interface DoubleGauge {
  /**
   * Set the gauge value.
   *
   * @param value The current gauge value.
   */
  void set(double value);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The current gauge value.
   * @param attributes A set of attributes to associate with the value.
   */
  void set(double value, Attributes attributes);

  /**
   * Records a value with a set of attributes.
   *
   * @param value The current gauge value.
   * @param attributes A set of attributes to associate with the value.
   * @param context The explicit context to associate with this measurement.
   */
  void set(double value, Attributes attributes, Context context);
}
