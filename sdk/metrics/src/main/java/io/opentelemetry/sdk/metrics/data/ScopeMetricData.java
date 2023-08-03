/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import javax.annotation.concurrent.Immutable;

/** Scope metric data represents the aggregated measurements of an instrument. */
@Immutable
public interface ScopeMetricData {
  /**
   * Returns the metric {@link InstrumentationScopeInfo}.
   *
   * <p>The {@link InstrumentationScopeInfo} is determined from the options used when {@link
   * MeterBuilder#build()}ing the {@link Meter}.
   */
  InstrumentationScopeInfo getInstrumentationScopeInfo();

  /**
   * Returns the metric name.
   *
   * <p>The metric name is typically the instrument name, but may be optionally overridden by a
   * {@link View}.
   */
  String getName();

  /**
   * Returns the metric description.
   *
   * <p>The metric name is typically the instrument description, but may be optionally overridden by
   * a {@link View}.
   */
  String getDescription();

  /** Returns the metric unit. */
  String getUnit();

  /** Returns the type of this metric. */
  MetricDataType getType();

  /**
   * Returns the unconstrained metric data.
   *
   * <p>Most will instead prefer to access the constrained metric data after first checking the
   * {@link #getType()}:
   *
   * <pre>{@code
   * //  if (ScopeMetricData.getType() == MetricDataType.LONG_SUM) {
   * //    SumData<LongPointData> sumData = ScopeMetricData.getLongSumData();
   * //    ... // Process long sum data
   * //  }
   * }</pre>
   *
   * @see #getDoubleGaugeData()
   * @see #getLongGaugeData()
   * @see #getDoubleSumData()
   * @see #getLongSumData()
   * @see #getHistogramData()
   * @see #getSummaryData()
   */
  Data<?> getData();

  /** Returns {@code true} if there are no points associated with this metric. */
  default boolean isEmpty() {
    return getData().getPoints().isEmpty();
  }

  /**
   * Returns the {@code double} {@link GaugeData} if type is {@link MetricDataType#DOUBLE_GAUGE},
   * otherwise a default empty data.
   */
  @SuppressWarnings("unchecked")
  default GaugeData<DoublePointData> getDoubleGaugeData() {
    if (getType() == MetricDataType.DOUBLE_GAUGE) {
      return (GaugeData<DoublePointData>) getData();
    }
    return ImmutableGaugeData.empty();
  }

  /**
   * Returns the {@code long} {@link GaugeData} if type is {@link MetricDataType#LONG_GAUGE},
   * otherwise a default empty data.
   */
  @SuppressWarnings("unchecked")
  default GaugeData<LongPointData> getLongGaugeData() {
    if (getType() == MetricDataType.LONG_GAUGE) {
      return (GaugeData<LongPointData>) getData();
    }
    return ImmutableGaugeData.empty();
  }

  /**
   * Returns the {@code double} {@link SumData} if type is {@link MetricDataType#DOUBLE_SUM},
   * otherwise a default empty data.
   */
  @SuppressWarnings("unchecked")
  default SumData<DoublePointData> getDoubleSumData() {
    if (getType() == MetricDataType.DOUBLE_SUM) {
      return (ImmutableSumData<DoublePointData>) getData();
    }
    return ImmutableSumData.empty();
  }

  /**
   * Returns the {@code long} {@link SumData} if type is {@link MetricDataType#LONG_SUM}, otherwise
   * a default empty data.
   */
  @SuppressWarnings("unchecked")
  default SumData<LongPointData> getLongSumData() {
    if (getType() == MetricDataType.LONG_SUM) {
      return (SumData<LongPointData>) getData();
    }
    return ImmutableSumData.empty();
  }

  /**
   * Returns the {@link SummaryData} if type is {@link MetricDataType#SUMMARY}, otherwise a default
   * empty data.
   */
  default SummaryData getSummaryData() {
    if (getType() == MetricDataType.SUMMARY) {
      return (SummaryData) getData();
    }
    return ImmutableSummaryData.empty();
  }

  /**
   * Returns the {@link HistogramData} if type is {@link MetricDataType#HISTOGRAM}, otherwise a
   * default empty data.
   */
  default HistogramData getHistogramData() {
    if (getType() == MetricDataType.HISTOGRAM) {
      return (HistogramData) getData();
    }
    return ImmutableHistogramData.empty();
  }

  /**
   * Returns the {@link ExponentialHistogramData} if type is {@link
   * MetricDataType#EXPONENTIAL_HISTOGRAM}, otherwise a default empty data.
   */
  default ExponentialHistogramData getExponentialHistogramData() {
    if (getType() == MetricDataType.EXPONENTIAL_HISTOGRAM) {
      return (ExponentialHistogramData) getData();
    }
    return ImmutableExponentialHistogramData.empty();
  }
}
