/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link ImmutableMetricData} represents the data exported as part of aggregating one {@code
 * Instrument}.
 */
@Immutable
public interface MetricData {

  /**
   * Returns the resource of this {@code MetricData}.
   *
   * @return the resource of this {@code MetricData}.
   */
  Resource getResource();

  /**
   * Returns the instrumentation library specified when creating the {@code Meter} which created the
   * {@code Instrument} that produces {@code MetricData}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the metric name.
   *
   * @return the metric name.
   */
  String getName();

  /**
   * Returns the description of this metric.
   *
   * @return the description of this metric.
   */
  String getDescription();

  /**
   * Returns the unit of this metric.
   *
   * @return the unit of this metric.
   */
  String getUnit();

  /**
   * Returns the type of this metric.
   *
   * @return the type of this metric.
   */
  MetricDataType getType();

  Data<?> getData();

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
  @SuppressWarnings("unchecked")
  default GaugeData<DoublePointData> getDoubleGaugeData() {
    if (getType() == MetricDataType.DOUBLE_GAUGE) {
      return (GaugeData<DoublePointData>) getData();
    }
    return ImmutableGaugeData.empty();
  }

  /**
   * Returns the {@code LongGaugeData} if type is {@link MetricDataType#LONG_GAUGE}, otherwise a
   * default empty data.
   *
   * @return the {@code LongGaugeData} if type is {@link MetricDataType#LONG_GAUGE}, otherwise a
   *     default empty data.
   */
  @SuppressWarnings("unchecked")
  default GaugeData<LongPointData> getLongGaugeData() {
    if (getType() == MetricDataType.LONG_GAUGE) {
      return (GaugeData<LongPointData>) getData();
    }
    return ImmutableGaugeData.empty();
  }

  /**
   * Returns the {@code DoubleSumData} if type is {@link MetricDataType#DOUBLE_SUM}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleSumData} if type is {@link MetricDataType#DOUBLE_SUM}, otherwise a
   *     default empty data.
   */
  @SuppressWarnings("unchecked")
  default SumData<DoublePointData> getDoubleSumData() {
    if (getType() == MetricDataType.DOUBLE_SUM) {
      return (ImmutableSumData<DoublePointData>) getData();
    }
    return ImmutableSumData.empty();
  }

  /**
   * Returns the {@code LongSumData} if type is {@link MetricDataType#LONG_SUM}, otherwise a default
   * empty data.
   *
   * @return the {@code LongSumData} if type is {@link MetricDataType#LONG_SUM}, otherwise a default
   *     empty data.
   */
  @SuppressWarnings("unchecked")
  default SumData<LongPointData> getLongSumData() {
    if (getType() == MetricDataType.LONG_SUM) {
      return (SumData<LongPointData>) getData();
    }
    return ImmutableSumData.empty();
  }

  /**
   * Returns the {@code DoubleSummaryData} if type is {@link MetricDataType#SUMMARY}, otherwise a
   * default empty data.
   *
   * @return the {@code DoubleSummaryData} if type is {@link MetricDataType#SUMMARY}, otherwise a
   *     default * empty data.
   */
  default SummaryData getSummaryData() {
    if (getType() == MetricDataType.SUMMARY) {
      return (SummaryData) getData();
    }
    return ImmutableSummaryData.empty();
  }

  /**
   * Returns the {@code DoubleHistogramData} if type is {@link MetricDataType#HISTOGRAM}, otherwise
   * a default empty data.
   *
   * @return the {@code DoubleHistogramData} if type is {@link MetricDataType#HISTOGRAM}, otherwise
   *     a default empty data.
   */
  default HistogramData getHistogramData() {
    if (getType() == MetricDataType.HISTOGRAM) {
      return (HistogramData) getData();
    }
    return ImmutableHistogramData.empty();
  }
}
