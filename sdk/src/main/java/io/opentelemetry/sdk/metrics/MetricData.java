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

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.internal.Utils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
   * Returns the data {@link Int64Point}s for this metric, or {@code null} if the {@link
   * Descriptor.Type} of the metric does not generate this type of points.
   *
   * <p>Only one type of points are available at any moment for a {@link MetricData}.
   *
   * @return the data {@link Point}s for this metric, or {@code null} if this type of points are not
   *     accepted.
   * @since 0.3.0
   */
  @Nullable
  public abstract Collection<Int64Point> getInt64Points();

  /**
   * Returns the data {@link DoublePoint}s for this metric, or {@code null} if the {@link
   * Descriptor.Type} of the metric does not generate this type of points.
   *
   * <p>Only one type of points are available at any moment for a {@link MetricData}.
   *
   * @return the data {@link Point}s for this metric, or {@code null} if this type of points are not
   *     accepted.
   * @since 0.3.0
   */
  @Nullable
  public abstract Collection<DoublePoint> getDoublePoints();

  static MetricData createWithInt64Points(
      Descriptor descriptor, Collection<Int64Point> int64Points) {
    Descriptor.Type type = Utils.checkNotNull(descriptor, "descriptor").getType();
    Utils.checkState(
        type == Descriptor.Type.NON_MONOTONIC_INT64 || type == Descriptor.Type.MONOTONIC_INT64,
        "Incompatible points type with metric type.");
    return new AutoValue_MetricData(
        descriptor, Utils.checkNotNull(int64Points, "longPoints"), null);
  }

  static MetricData createWithDoublePoints(
      Descriptor descriptor, Collection<DoublePoint> doublePoints) {
    Descriptor.Type type = Utils.checkNotNull(descriptor, "descriptor").getType();
    Utils.checkState(
        type == Descriptor.Type.NON_MONOTONIC_DOUBLE || type == Descriptor.Type.MONOTONIC_DOUBLE,
        "Incompatible points type with metric type.");
    return new AutoValue_MetricData(
        descriptor, null, Utils.checkNotNull(doublePoints, "doublePoints"));
  }

  @Immutable
  abstract static class Point {
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
     * Returns the the epoch timestamp in nanos when data were collected, usually it represents the
     * moment when {@code Instrument.getData()} was called.
     *
     * @return the epoch timestamp in nanos.
     * @since 0.3.0
     */
    public abstract long getEpochNanos();
  }

  /**
   * Int64Point is a single data point in a timeseries that describes the time-varying values of a
   * int64 metric.
   */
  @Immutable
  @AutoValue
  public abstract static class Int64Point extends Point {
    Int64Point() {}

    /**
     * Returns the value of the data point.
     *
     * @return the value of the data point.
     */
    public abstract long getValue();

    static Int64Point createInternal(long startEpochNanos, long epochNanos, long value) {
      return new AutoValue_MetricData_Int64Point(startEpochNanos, epochNanos, value);
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

    static DoublePoint createInternal(long startEpochNanos, long epochNanos, double value) {
      return new AutoValue_MetricData_DoublePoint(startEpochNanos, epochNanos, value);
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
       * An instantaneous measurement of an int64 value.
       *
       * @since 0.1.0
       */
      NON_MONOTONIC_INT64,

      /**
       * An instantaneous measurement of a double value.
       *
       * @since 0.1.0
       */
      NON_MONOTONIC_DOUBLE,

      /**
       * An cumulative measurement of an int64 value.
       *
       * @since 0.1.0
       */
      MONOTONIC_INT64,

      /**
       * An cumulative measurement of a double value.
       *
       * @since 0.1.0
       */
      MONOTONIC_DOUBLE,
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
     * Returns the label keys associated with this metric descriptor.
     *
     * @return the label keys associated with this metric descriptor.
     * @since 0.1.0
     */
    public abstract List<String> getLabelKeys();

    /**
     * Returns the constant labels associated with this metric descriptor.
     *
     * @return the constant labels associated with this metric descriptor.
     * @since 0.1.0
     */
    public abstract Map<String, String> getConstantLabels();

    static Descriptor createInternal(
        String name,
        String description,
        String unit,
        Type type,
        List<String> labelKeys,
        Map<String, String> constantLabels) {
      return new AutoValue_MetricData_Descriptor(
          name, description, unit, type, labelKeys, constantLabels);
    }
  }
}
