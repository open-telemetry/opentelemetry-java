/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
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
  private static final DoubleHistogramData DEFAULT_DOUBLE_HISTOGRAM_DATA =
      DoubleHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

  /**
   * Returns a new MetricData wih a {@link MetricDataType#DOUBLE_GAUGE} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#DOUBLE_GAUGE} type.
   */
  public static MetricData createDoubleGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleGaugeData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.DOUBLE_GAUGE,
        data);
  }

  /**
   * Returns a new MetricData wih a {@link MetricDataType#LONG_GAUGE} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#LONG_GAUGE} type.
   */
  public static MetricData createLongGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongGaugeData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.LONG_GAUGE,
        data);
  }

  /**
   * Returns a new MetricData wih a {@link MetricDataType#DOUBLE_SUM} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#DOUBLE_SUM} type.
   */
  public static MetricData createDoubleSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSumData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.DOUBLE_SUM,
        data);
  }

  /**
   * Returns a new MetricData wih a {@link MetricDataType#LONG_SUM} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#LONG_SUM} type.
   */
  public static MetricData createLongSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongSumData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.LONG_SUM,
        data);
  }

  /**
   * Returns a new MetricData wih a {@link MetricDataType#SUMMARY} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#SUMMARY} type.
   */
  public static MetricData createDoubleSummary(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSummaryData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.SUMMARY,
        data);
  }

  /**
   * Returns a new MetricData with a {@link MetricDataType#HISTOGRAM} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#HISTOGRAM} type.
   */
  public static MetricData createDoubleHistogram(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleHistogramData data) {
    return new AutoValue_MetricData(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.HISTOGRAM,
        data);
  }

  MetricData() {}

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
  public abstract MetricDataType getType();

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
   * Returns the {@code DoubleGaugeData} if type is {@link MetricDataType#DOUBLE_GAUGE}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleGaugeData} if type is {@link MetricDataType#DOUBLE_GAUGE}, otherwise a
   *     default empty data.
   */
  public final DoubleGaugeData getDoubleGaugeData() {
    if (getType() == MetricDataType.DOUBLE_GAUGE) {
      return (DoubleGaugeData) getData();
    }
    return DEFAULT_DOUBLE_GAUGE_DATA;
  }

  /**
   * Returns the {@code LongGaugeData} if type is {@link MetricDataType#LONG_GAUGE}, otherwise a
   * default empty data.
   *
   * @return the {@code LongGaugeData} if type is {@link MetricDataType#LONG_GAUGE}, otherwise a
   *     default empty data.
   */
  public final LongGaugeData getLongGaugeData() {
    if (getType() == MetricDataType.LONG_GAUGE) {
      return (LongGaugeData) getData();
    }
    return DEFAULT_LONG_GAUGE_DATA;
  }

  /**
   * Returns the {@code DoubleSumData} if type is {@link MetricDataType#DOUBLE_SUM}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleSumData} if type is {@link MetricDataType#DOUBLE_SUM}, otherwise a
   *     default empty data.
   */
  public final DoubleSumData getDoubleSumData() {
    if (getType() == MetricDataType.DOUBLE_SUM) {
      return (DoubleSumData) getData();
    }
    return DEFAULT_DOUBLE_SUM_DATA;
  }

  /**
   * Returns the {@code LongSumData} if type is {@link MetricDataType#LONG_SUM}, otherwise a default
   * empty data.
   *
   * @return the {@code LongSumData} if type is {@link MetricDataType#LONG_SUM}, otherwise a default
   *     empty data.
   */
  public final LongSumData getLongSumData() {
    if (getType() == MetricDataType.LONG_SUM) {
      return (LongSumData) getData();
    }
    return DEFAULT_LONG_SUM_DATA;
  }

  /**
   * Returns the {@code DoubleSummaryData} if type is {@link MetricDataType#SUMMARY}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleSummaryData} if type is {@link MetricDataType#SUMMARY}, otherwise a
   *     default * empty data.
   */
  public final DoubleSummaryData getDoubleSummaryData() {
    if (getType() == MetricDataType.SUMMARY) {
      return (DoubleSummaryData) getData();
    }
    return DEFAULT_DOUBLE_SUMMARY_DATA;
  }

  /**
   * Returns the {@code DoubleHistogramData} if type is {@link MetricDataType#HISTOGRAM}, otherwise
   * a default empty data.
   *
   * @return the {@code DoubleHistogramData} if type is {@link MetricDataType#HISTOGRAM}, otherwise
   *     a default empty data.
   */
  public final DoubleHistogramData getDoubleHistogramData() {
    if (getType() == MetricDataType.HISTOGRAM) {
      return (DoubleHistogramData) getData();
    }
    return DEFAULT_DOUBLE_HISTOGRAM_DATA;
  }
}
