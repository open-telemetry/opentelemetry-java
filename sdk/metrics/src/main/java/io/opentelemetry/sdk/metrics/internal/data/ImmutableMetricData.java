/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * A container of metrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableMetricData implements MetricData {

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
      GaugeData<DoublePointData> data) {
    return create(
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
      GaugeData<LongPointData> data) {
    return create(
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
      SumData<DoublePointData> data) {
    return create(
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
      SumData<LongPointData> data) {
    return create(
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
      SummaryData data) {
    return create(
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
      HistogramData data) {
    return create(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.HISTOGRAM,
        data);
  }

  /**
   * Returns a new MetricData with a {@link MetricDataType#EXPONENTIAL_HISTOGRAM} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#EXPONENTIAL_HISTOGRAM} type.
   */
  public static MetricData createExponentialHistogram(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      ExponentialHistogramData data) {
    return create(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.EXPONENTIAL_HISTOGRAM,
        data);
  }

  // Visible for testing
  static ImmutableMetricData create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      MetricDataType type,
      Data<?> data) {
    return new AutoValue_ImmutableMetricData(
        resource, instrumentationLibraryInfo, name, description, unit, type, data);
  }

  ImmutableMetricData() {}
}
