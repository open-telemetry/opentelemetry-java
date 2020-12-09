/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link MetricData} represents the data exported as part of aggregating one {@code Instrument}.
 */
@Immutable
@AutoValue
public abstract class MetricData {

  MetricData() {}

  /** The kind of metric. It describes how the data is reported. */
  public enum Type {
    /**
     * A Gauge represents a measurement of a long value at a moment in time. Generally only one
     * instance of a given Gauge metric will be reported per reporting interval.
     */
    GAUGE_LONG,

    /**
     * A Gauge represents a measurement of a double value at a moment in time. Generally only one
     * instance of a given Gauge metric will be reported per reporting interval.
     */
    GAUGE_DOUBLE,

    /** A sum of long (int64) values. Reports {@link LongPoint} points. */
    NON_MONOTONIC_SUM_LONG,

    /** A sum of double values. Reports {@link DoublePoint} points. */
    NON_MONOTONIC_SUM_DOUBLE,

    /** A sum of non negative long (int64) values. Reports {@link LongPoint} points. */
    SUM_LONG,

    /** A sum of non negative double values. Reports {@link DoublePoint} points. */
    SUM_DOUBLE,

    /**
     * A Summary of measurements of numeric values, containing the minimum value recorded, the
     * maximum value recorded, the sum of all measurements and the total number of measurements
     * recorded.
     */
    SUMMARY,
  }

  /**
   * Returns the resource of this {@code MetricData}.
   *
   * @return the resource of this {@code MetricData}.
   */
  public abstract Resource getResource();

  /**
   * Returns the instrumentation library specified when creating the {@code Meter} which created the
   * {@code Instrument} that produces {@code MetricData}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  public abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the metric name.
   *
   * @return the metric name.
   */
  public abstract String getName();

  /**
   * Returns the description of this metric.
   *
   * @return the description of this metric.
   */
  public abstract String getDescription();

  /**
   * Returns the unit of this metric.
   *
   * @return the unit of this metric.
   */
  public abstract String getUnit();

  /**
   * Returns the type of this metric.
   *
   * @return the type of this metric.
   */
  public abstract Type getType();

  /**
   * Returns the data {@link Point}s for this metric.
   *
   * <p>Only one type of points are available at any moment for a {@link MetricData}, and the type
   * is determined by the {@link Type}.
   *
   * @return the data {@link Point}s for this metric, or empty {@code Collection} if no points.
   */
  public abstract Collection<Point> getPoints();

  public static MetricData create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      Type type,
      Collection<Point> points) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, type, points);
  }

  @Immutable
  public abstract static class Point {

    Point() {}

    /**
     * Returns the start epoch timestamp in nanos of this {@code Instrument}, usually the time when
     * the metric was created or an aggregation was enabled.
     *
     * @return the start epoch timestamp in nanos.
     */
    public abstract long getStartEpochNanos();

    /**
     * Returns the epoch timestamp in nanos when data were collected, usually it represents the
     * moment when {@code Instrument.getData()} was called.
     *
     * @return the epoch timestamp in nanos.
     */
    public abstract long getEpochNanos();

    /**
     * Returns the labels associated with this {@code Point}.
     *
     * @return the labels associated with this {@code Point}.
     */
    public abstract Labels getLabels();
  }

  /**
   * LongPoint is a single data point in a timeseries that describes the time-varying values of a
   * int64 metric.
   *
   * <p>In the proto definition this is called Int64Point.
   */
  @Immutable
  @AutoValue
  public abstract static class LongPoint extends Point {

    LongPoint() {}

    /**
     * Returns the value of the data point.
     *
     * @return the value of the data point.
     */
    public abstract long getValue();

    public static LongPoint create(
        long startEpochNanos, long epochNanos, Labels labels, long value) {
      return new AutoValue_MetricData_LongPoint(startEpochNanos, epochNanos, labels, value);
    }
  }

  /**
   * DoublePoint is a single data point in a timeseries that describes the time-varying value of a
   * double metric.
   */
  @Immutable
  @AutoValue
  public abstract static class DoublePoint extends Point {

    DoublePoint() {}

    /**
     * Returns the value of the data point.
     *
     * @return the value of the data point.
     */
    public abstract double getValue();

    public static DoublePoint create(
        long startEpochNanos, long epochNanos, Labels labels, double value) {
      return new AutoValue_MetricData_DoublePoint(startEpochNanos, epochNanos, labels, value);
    }
  }

  /**
   * SummaryPoint is a single data point that summarizes the values in a time series of numeric
   * values.
   */
  @Immutable
  @AutoValue
  public abstract static class SummaryPoint extends Point {

    SummaryPoint() {}

    /**
     * The number of values that are being summarized.
     *
     * @return the number of values that are being summarized.
     */
    public abstract long getCount();

    /**
     * The sum of all the values that are being summarized.
     *
     * @return the sum of the values that are being summarized.
     */
    public abstract double getSum();

    /**
     * Percentile values in the summarization. Note: a percentile 0.0 represents the minimum value
     * in the distribution.
     *
     * @return the percentiles values.
     */
    public abstract List<ValueAtPercentile> getPercentileValues();

    public static SummaryPoint create(
        long startEpochNanos,
        long epochNanos,
        Labels labels,
        long count,
        double sum,
        List<ValueAtPercentile> percentileValues) {
      return new AutoValue_MetricData_SummaryPoint(
          startEpochNanos, epochNanos, labels, count, sum, percentileValues);
    }
  }

  @Immutable
  @AutoValue
  public abstract static class ValueAtPercentile {

    ValueAtPercentile() {}

    /**
     * The percentile of a distribution. Must be in the interval [0.0, 100.0].
     *
     * @return the percentile.
     */
    public abstract double getPercentile();

    /**
     * The value at the given percentile of a distribution.
     *
     * @return the value at the percentile.
     */
    public abstract double getValue();

    public static ValueAtPercentile create(double percentile, double value) {
      return new AutoValue_MetricData_ValueAtPercentile(percentile, value);
    }
  }
}
