/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;

/** An interface for observing measurements with {@code long} values. */
public interface ObservableLongMeasurement extends ObservableMeasurement {
  /**
   * Records a measurement.
   *
   * @param value The measurement amount.
   * @deprecated Use {@link #record(long)}.
   */
  @Deprecated
  default void observe(long value) {
    record(value);
  }

  /**
   * Records a measurement with a set of attributes.
   *
   * @param value The measurement amount.
   * @param attributes A set of attributes to associate with the count.
   * @deprecated Use {@link #record(long, Attributes)}.
   */
  @Deprecated
  default void observe(long value, Attributes attributes) {
    record(value, attributes);
  }

  /**
   * Records a measurement.
   *
   * @param value The measurement amount.
   */
  void record(long value);

  /**
   * Records a measurement with a set of attributes.
   *
   * @param value The measurement amount.
   * @param attributes A set of attributes to associate with the count.
   */
  void record(long value, Attributes attributes);
}
