/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/** The kind of metric. It describes how the data is reported. */
public enum MetricDataType {
  /**
   * A Gauge represents a measurement of a long value at a moment in time. Generally only one
   * instance of a given Gauge metric will be reported per reporting interval.
   */
  LONG_GAUGE,

  /**
   * A Gauge represents a measurement of a double value at a moment in time. Generally only one
   * instance of a given Gauge metric will be reported per reporting interval.
   */
  DOUBLE_GAUGE,

  /** A sum of non negative long (int64) values. Reports {@link LongSumData} data. */
  LONG_SUM,

  /** A sum of non negative double values. Reports {@link DoubleSumData} data. */
  DOUBLE_SUM,

  /**
   * A Summary of measurements of numeric values, containing the minimum value recorded, the maximum
   * value recorded, the sum of all measurements and the total number of measurements recorded.
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
