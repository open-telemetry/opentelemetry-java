/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/** A gauge instrument that synchronously records {@code double} values. */
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

  // TODO(jack-berg): should we add overload with Context argument?
}
