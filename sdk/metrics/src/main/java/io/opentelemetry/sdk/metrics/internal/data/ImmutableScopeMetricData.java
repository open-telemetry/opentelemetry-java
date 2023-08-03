/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.ScopeMetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import javax.annotation.concurrent.Immutable;

/**
 * A container of metrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableScopeMetricData implements ScopeMetricData {

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#DOUBLE_GAUGE} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#DOUBLE_GAUGE} type.
   */
  public static ScopeMetricData createDoubleGauge(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      GaugeData<DoublePointData> data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.DOUBLE_GAUGE, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#LONG_GAUGE} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#LONG_GAUGE} type.
   */
  public static ScopeMetricData createLongGauge(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      GaugeData<LongPointData> data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.LONG_GAUGE, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#DOUBLE_SUM} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#DOUBLE_SUM} type.
   */
  public static ScopeMetricData createDoubleSum(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      SumData<DoublePointData> data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.DOUBLE_SUM, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#LONG_SUM} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#LONG_SUM} type.
   */
  public static ScopeMetricData createLongSum(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      SumData<LongPointData> data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.LONG_SUM, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#SUMMARY} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#SUMMARY} type.
   */
  public static ScopeMetricData createDoubleSummary(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      SummaryData data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.SUMMARY, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#HISTOGRAM} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#HISTOGRAM} type.
   */
  public static ScopeMetricData createDoubleHistogram(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      HistogramData data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo, name, description, unit, MetricDataType.HISTOGRAM, data);
  }

  /**
   * Returns a new ScopeMetricData with a {@link MetricDataType#EXPONENTIAL_HISTOGRAM} type.
   *
   * @return a new ScopeMetricData with a {@link MetricDataType#EXPONENTIAL_HISTOGRAM} type.
   */
  public static ScopeMetricData createExponentialHistogram(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      ExponentialHistogramData data) {
    return ImmutableScopeMetricData.create(
        instrumentationScopeInfo,
        name,
        description,
        unit,
        MetricDataType.EXPONENTIAL_HISTOGRAM,
        data);
  }

  // Visible for testing
  static ImmutableScopeMetricData create(
      InstrumentationScopeInfo instrumentationScopeInfo,
      String name,
      String description,
      String unit,
      MetricDataType type,
      Data<?> data) {
    return new AutoValue_ImmutableScopeMetricData(
        instrumentationScopeInfo, name, description, unit, type, data);
  }

  ImmutableScopeMetricData() {}
}
