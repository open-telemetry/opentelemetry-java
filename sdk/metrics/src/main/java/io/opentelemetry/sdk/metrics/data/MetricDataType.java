/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/**
 * The type of metric {@link PointData}.
 *
 * @since 1.14.0
 */
public enum MetricDataType {
  /** A Gauge represents a measurement of a long value at a moment in time. */
  LONG_GAUGE,

  /**
   * A Gauge represents a measurement of a double value at a moment in time. Generally only one
   * instance of a given Gauge metric will be reported per reporting interval.
   */
  DOUBLE_GAUGE,

  /** A Sum of long (int64) values. */
  LONG_SUM,

  /** A Sum of double values. */
  DOUBLE_SUM,

  /**
   * A Summary of measurements of numeric values, the sum of all measurements and the total number
   * of measurements recorded, and quantiles describing the distribution of measurements (often
   * including minimum "0.0" and maximum "1.0" quantiles).
   */
  SUMMARY,

  /**
   * A Histogram represents an approximate representation of the distribution of measurements
   * recorded.
   */
  HISTOGRAM,

  /**
   * An Exponential Histogram represents an approximate representation of the distribution of
   * measurements recorded. The bucket boundaries follow a pre-determined exponential formula.
   */
  EXPONENTIAL_HISTOGRAM,
}
