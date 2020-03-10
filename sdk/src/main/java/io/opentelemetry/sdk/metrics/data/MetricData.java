/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link MetricData} represents the data exported as part of aggregating one {@code Instrument}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class MetricData {
  MetricData() {}

  /**
   * Returns the {@link Descriptor} of this metric.
   *
   * @return the {@code Descriptor} of this metric.
   * @since 0.1.0
   */
  public abstract Descriptor getDescriptor();

  /**
   * Returns the resource of this {@code MetricData}.
   *
   * @return the resource of this {@code MetricData}.
   * @since 0.1.0
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
   * Returns the data {@link Point}s for this metric.
   *
   * <p>Only one type of points are available at any moment for a {@link MetricData}, and the type
   * is determined by the {@link Descriptor.Type}.
   *
   * @return the data {@link Point}s for this metric, or empty {@code Collection} if no points.
   * @since 0.3.0
   */
  public abstract Collection<Point> getPoints();

  public static MetricData create(
      Descriptor descriptor,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Collection<Point> points) {
    return new AutoValue_MetricData(descriptor, resource, instrumentationLibraryInfo, points);
  }

  @Immutable
  public abstract static class Point {
    Point() {}

    /**
     * Returns the start epoch timestamp in nanos of this {@code Instrument}, usually the time when
     * the metric was created or an aggregation was enabled.
     *
     * @return the start epoch timestamp in nanos.
     * @since 0.3.0
     */
    public abstract long getStartEpochNanos();

    /**
     * Returns the epoch timestamp in nanos when data were collected, usually it represents the
     * moment when {@code Instrument.getData()} was called.
     *
     * @return the epoch timestamp in nanos.
     * @since 0.3.0
     */
    public abstract long getEpochNanos();

    /**
     * Returns the labels associated with this {@code Point}.
     *
     * @return the labels associated with this {@code Point}.
     */
    public abstract Map<String, String> getLabels();
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
        long startEpochNanos, long epochNanos, Map<String, String> labels, long value) {
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
        long startEpochNanos, long epochNanos, Map<String, String> labels, double value) {
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
        Map<String, String> labels,
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

  /**
   * {@link Descriptor} defines metadata about the {@code MetricData} type and its schema.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Descriptor {

    Descriptor() {}

    /**
     * The kind of metric. It describes how the data is reported.
     *
     * @since 0.1.0
     */
    public enum Type {

      /**
       * An instantaneous measurement of a long (int64) value. Reports {@link LongPoint} points.
       *
       * @since 0.1.0
       */
      NON_MONOTONIC_LONG,

      /**
       * An instantaneous measurement of a double value. Reports {@link DoublePoint} points.
       *
       * @since 0.1.0
       */
      NON_MONOTONIC_DOUBLE,

      /**
       * An cumulative measurement of an long (int64) value. Reports {@link LongPoint} points.
       *
       * @since 0.1.0
       */
      MONOTONIC_LONG,

      /**
       * An cumulative measurement of a double value. Reports {@link DoublePoint} points.
       *
       * @since 0.1.0
       */
      MONOTONIC_DOUBLE,

      /**
       * A Summary of measurements of numeric values, containing the minimum value recorded, the
       * maximum value recorded, the sum of all measurements and the total number of measurements
       * recorded.
       */
      SUMMARY,
    }

    /**
     * Returns the metric descriptor name.
     *
     * @return the metric descriptor name.
     * @since 0.1.0
     */
    public abstract String getName();

    /**
     * Returns the description of this metric descriptor.
     *
     * @return the description of this metric descriptor.
     * @since 0.1.0
     */
    public abstract String getDescription();

    /**
     * Returns the unit of this metric descriptor.
     *
     * @return the unit of this metric descriptor.
     * @since 0.1.0
     */
    public abstract String getUnit();

    /**
     * Returns the type of this metric descriptor.
     *
     * @return the type of this metric descriptor.
     * @since 0.1.0
     */
    public abstract Type getType();

    /**
     * Returns the constant labels associated with this metric descriptor.
     *
     * @return the constant labels associated with this metric descriptor.
     * @since 0.1.0
     */
    public abstract Map<String, String> getConstantLabels();

    public static Descriptor create(
        String name,
        String description,
        String unit,
        Type type,
        Map<String, String> constantLabels) {
      return new AutoValue_MetricData_Descriptor(name, description, unit, type, constantLabels);
    }
  }
}
