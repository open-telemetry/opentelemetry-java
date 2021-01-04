/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.Instrument;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link MetricData} represents the data exported as part of aggregating one {@code Instrument}.
 */
@Immutable
@AutoValue
public abstract class MetricData {
  private static final DoubleGaugeData DEFAULT_DOUBLE_GAUGE_DATA =
      DoubleGaugeData.create(Collections.emptyList());
  private static final LongGaugeData DEFAULT_LONG_GAUGE_DATA =
      LongGaugeData.create(Collections.emptyList());
  private static final DoubleSumData DEFAULT_DOUBLE_SUM_DATA =
      DoubleSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());
  private static final LongSumData DEFAULT_LONG_SUM_DATA =
      LongSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());
  private static final DoubleSummaryData DEFAULT_DOUBLE_SUMMARY_DATA =
      DoubleSummaryData.create(Collections.emptyList());

  public static MetricData createDoubleGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleGaugeData data) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, Type.DOUBLE_GAUGE, data);
  }

  public static MetricData createLongGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongGaugeData data) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, Type.LONG_GAUGE, data);
  }

  public static MetricData createDoubleSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSumData data) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, Type.DOUBLE_SUM, data);
  }

  public static MetricData createLongSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongSumData data) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, Type.LONG_SUM, data);
  }

  public static MetricData createDoubleSummary(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSummaryData data) {
    return new AutoValue_MetricData(
        resource, instrumentationLibraryInfo, name, description, unit, Type.SUMMARY, data);
  }

  MetricData() {}

  /** The kind of metric. It describes how the data is reported. */
  public enum Type {
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
     * A Summary of measurements of numeric values, containing the minimum value recorded, the
     * maximum value recorded, the sum of all measurements and the total number of measurements
     * recorded.
     */
    SUMMARY,
  }

  /** An enumeration which describes the time period over which metrics should be aggregated. */
  public enum AggregationTemporality {
    /** Metrics will be aggregated only over the most recent collection interval. */
    DELTA,
    /** Metrics will be aggregated over the lifetime of the associated {@link Instrument}. */
    CUMULATIVE
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

  abstract Data<?> getData();

  /**
   * Returns {@code true} if there are no points associated with this metric.
   *
   * @return {@code true} if there are no points associated with this metric.
   */
  public boolean isEmpty() {
    return getData().getPoints().isEmpty();
  }

  /**
   * Returns the {@code DoubleGaugeData} if type is {@link Type#DOUBLE_GAUGE}, otherwise a default
   * empty data.
   *
   * @return the {@code DoubleGaugeData} if type is {@link Type#DOUBLE_GAUGE}, otherwise a default
   *     empty data.
   */
  public final DoubleGaugeData getDoubleGaugeData() {
    if (getType() == Type.DOUBLE_GAUGE) {
      return (DoubleGaugeData) getData();
    }
    return DEFAULT_DOUBLE_GAUGE_DATA;
  }

  /**
   * Returns the {@code LongGaugeData} if type is {@link Type#LONG_GAUGE}, otherwise a default empty
   * data.
   *
   * @return the {@code LongGaugeData} if type is {@link Type#LONG_GAUGE}, otherwise a default empty
   *     data.
   */
  public final LongGaugeData getLongGaugeData() {
    if (getType() == Type.LONG_GAUGE) {
      return (LongGaugeData) getData();
    }
    return DEFAULT_LONG_GAUGE_DATA;
  }

  /**
   * Returns the {@code DoubleSumData} if type is {@link Type#DOUBLE_SUM}, otherwise a default empty
   * data.
   *
   * @return the {@code DoubleSumData} if type is {@link Type#DOUBLE_SUM}, otherwise a default empty
   *     data.
   */
  public final DoubleSumData getDoubleSumData() {
    if (getType() == Type.DOUBLE_SUM) {
      return (DoubleSumData) getData();
    }
    return DEFAULT_DOUBLE_SUM_DATA;
  }

  /**
   * Returns the {@code LongSumData} if type is {@link Type#LONG_SUM}, otherwise a default empty
   * data.
   *
   * @return the {@code LongSumData} if type is {@link Type#LONG_SUM}, otherwise a default empty
   *     data.
   */
  public final LongSumData getLongSumData() {
    if (getType() == Type.LONG_SUM) {
      return (LongSumData) getData();
    }
    return DEFAULT_LONG_SUM_DATA;
  }

  /**
   * Returns the {@code DoubleSummaryData} if type is {@link Type#SUMMARY}, otherwise a default
   * empty data.
   *
   * @return the {@code DoubleSummaryData} if type is {@link Type#SUMMARY}, otherwise a default *
   *     empty data.
   */
  public final DoubleSummaryData getDoubleSummaryData() {
    if (getType() == Type.SUMMARY) {
      return (DoubleSummaryData) getData();
    }
    return DEFAULT_DOUBLE_SUMMARY_DATA;
  }

  @Immutable
  abstract static class Data<T extends Point> {
    /**
     * Returns the data {@link Point}s for this metric.
     *
     * @return the data {@link Point}s for this metric, or empty {@code Collection} if no points.
     */
    public abstract Collection<T> getPoints();
  }

  @Immutable
  @AutoValue
  public abstract static class DoubleGaugeData extends Data<DoublePoint> {
    public static DoubleGaugeData create(Collection<DoublePoint> points) {
      return new AutoValue_MetricData_DoubleGaugeData(points);
    }

    @Override
    public abstract Collection<DoublePoint> getPoints();
  }

  @Immutable
  @AutoValue
  public abstract static class LongGaugeData extends Data<LongPoint> {
    public static LongGaugeData create(Collection<LongPoint> points) {
      return new AutoValue_MetricData_LongGaugeData(points);
    }

    @Override
    public abstract Collection<LongPoint> getPoints();
  }

  @Immutable
  abstract static class SumData<T extends Point> extends Data<T> {
    /**
     * Returns "true" if the sum is monotonic.
     *
     * @return "true" if the sum is monotonic
     */
    public abstract boolean isMonotonic();

    /**
     * Returns the {@code AggregationTemporality} of this metric,
     *
     * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
     * time, or cumulative changes since a fixed start time.
     *
     * @return the {@code AggregationTemporality} of this metric
     */
    public abstract AggregationTemporality getAggregationTemporality();
  }

  @Immutable
  @AutoValue
  public abstract static class DoubleSumData extends SumData<DoublePoint> {
    public static DoubleSumData create(
        boolean isMonotonic, AggregationTemporality temporality, Collection<DoublePoint> points) {
      return new AutoValue_MetricData_DoubleSumData(isMonotonic, temporality, points);
    }

    @Override
    public abstract Collection<DoublePoint> getPoints();
  }

  @Immutable
  @AutoValue
  public abstract static class LongSumData extends SumData<LongPoint> {
    public static LongSumData create(
        boolean isMonotonic, AggregationTemporality temporality, Collection<LongPoint> points) {
      return new AutoValue_MetricData_LongSumData(isMonotonic, temporality, points);
    }

    @Override
    public abstract Collection<LongPoint> getPoints();
  }

  @Immutable
  @AutoValue
  public abstract static class DoubleSummaryData extends Data<DoubleSummaryPoint> {
    public static DoubleSummaryData create(Collection<DoubleSummaryPoint> points) {
      return new AutoValue_MetricData_DoubleSummaryData(points);
    }

    @Override
    public abstract Collection<DoubleSummaryPoint> getPoints();
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
  public abstract static class DoubleSummaryPoint extends Point {

    DoubleSummaryPoint() {}

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

    public static DoubleSummaryPoint create(
        long startEpochNanos,
        long epochNanos,
        Labels labels,
        long count,
        double sum,
        List<ValueAtPercentile> percentileValues) {
      return new AutoValue_MetricData_DoubleSummaryPoint(
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
