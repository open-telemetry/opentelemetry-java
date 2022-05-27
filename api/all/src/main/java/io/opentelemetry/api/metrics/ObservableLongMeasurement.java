/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;

/**
 * An interface for observing measurements with {@code long} values.
 *
 * @since 1.10.0
 */
public interface ObservableLongMeasurement extends ObservableMeasurement {

  /**
   * Records a measurement.
   *
   * @param value The measurement value.
   */
  void record(long value);

  /**
   * Records a measurement with a set of attributes.
   *
   * @param value The measurement value.
   * @param attributes A set of attributes to associate with the value.
   */
  void record(long value, Attributes attributes);
}
