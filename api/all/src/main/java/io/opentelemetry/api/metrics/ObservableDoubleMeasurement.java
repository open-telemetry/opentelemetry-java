/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;

/** An interface for observing measurements with {@code double} values. */
public interface ObservableDoubleMeasurement extends ObservableMeasurement {
  /**
   * Records a measurement.
   *
   * @param value The measurement amount.
   */
  default void observe(double value) {
    record(value);
  }

  /**
   * Records a measurement with a set of attributes.
   *
   * @param value The measurement amount.
   * @param attributes A set of attributes to associate with the count.
   */
  default void observe(double value, Attributes attributes) {
    record(value, attributes);
  }

  /**
   * Records a measurement.
   *
   * @param value The measurement amount.
   */
  void record(double value);

  /**
   * Records a measurement with a set of attributes.
   *
   * @param value The measurement amount.
   * @param attributes A set of attributes to associate with the count.
   */
  void record(double value, Attributes attributes);
}
