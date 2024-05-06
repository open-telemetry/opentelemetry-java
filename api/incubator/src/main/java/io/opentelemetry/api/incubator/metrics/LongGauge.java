/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/** A gauge instrument that synchronously records {@code long} values. */
@ThreadSafe
public interface LongGauge {
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

  // TODO(jack-berg): should we add overload with Context argument?
}
