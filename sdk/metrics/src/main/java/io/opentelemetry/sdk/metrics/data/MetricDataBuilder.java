/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class MetricDataBuilder implements MetricData {
  /**
   * Returns a new MetricData wih a {@link MetricDataType#DOUBLE_GAUGE} type.
   *
   * @return a new MetricData wih a {@link MetricDataType#DOUBLE_GAUGE} type.
   */
  public static MetricDataBuilder createDoubleGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleGaugeData data) {
    return new AutoValue_MetricDataBuilder(
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
  public static MetricDataBuilder createLongGauge(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongGaugeData data) {
    return new AutoValue_MetricDataBuilder(
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
  public static MetricDataBuilder createDoubleSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSumData data) {
    return new AutoValue_MetricDataBuilder(
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
  public static MetricDataBuilder createLongSum(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      LongSumData data) {
    return new AutoValue_MetricDataBuilder(
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
  public static MetricDataBuilder createDoubleSummary(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleSummaryData data) {
    return new AutoValue_MetricDataBuilder(
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
  public static MetricDataBuilder createDoubleHistogram(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      DoubleHistogramData data) {
    return new AutoValue_MetricDataBuilder(
        resource,
        instrumentationLibraryInfo,
        name,
        description,
        unit,
        MetricDataType.HISTOGRAM,
        data);
  }

  MetricDataBuilder() {}
}
