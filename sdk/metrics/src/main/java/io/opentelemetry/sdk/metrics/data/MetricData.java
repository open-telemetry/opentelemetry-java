/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;

/**
 * A {@link MetricDataBuilder} represents the data exported as part of aggregating one {@code
 * Instrument}.
 */
public interface MetricData {

  DoubleGaugeData DEFAULT_DOUBLE_GAUGE_DATA = DoubleGaugeData.create(Collections.emptyList());
  LongGaugeData DEFAULT_LONG_GAUGE_DATA = LongGaugeData.create(Collections.emptyList());
  DoubleSumData DEFAULT_DOUBLE_SUM_DATA =
      DoubleSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());
  LongSumData DEFAULT_LONG_SUM_DATA =
      LongSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());
  DoubleSummaryData DEFAULT_DOUBLE_SUMMARY_DATA = DoubleSummaryData.create(Collections.emptyList());
  DoubleHistogramData DEFAULT_DOUBLE_HISTOGRAM_DATA =
      DoubleHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

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
  default boolean isEmpty() {
    return getData().getPoints().isEmpty();
  }

  /**
   * Returns the {@code DoubleGaugeData} if type is {@link MetricDataType#DOUBLE_GAUGE}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleGaugeData} if type is {@link MetricDataType#DOUBLE_GAUGE}, otherwise a
   *     default empty data.
   */
  default DoubleGaugeData getDoubleGaugeData() {
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
  default LongGaugeData getLongGaugeData() {
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
  default DoubleSumData getDoubleSumData() {
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
  default LongSumData getLongSumData() {
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
  default DoubleSummaryData getDoubleSummaryData() {
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
  default DoubleHistogramData getDoubleHistogramData() {
    if (getType() == MetricDataType.HISTOGRAM) {
      return (DoubleHistogramData) getData();
    }
    return DEFAULT_DOUBLE_HISTOGRAM_DATA;
  }
}
