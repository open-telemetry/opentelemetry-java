/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;

// Four types we use are same in prometheus and openmetrics format
enum PrometheusType {
  GAUGE("gauge"),
  COUNTER("counter"),
  SUMMARY("summary"),
  HISTOGRAM("histogram");

  private final String typeString;

  PrometheusType(String typeString) {
    this.typeString = typeString;
  }

  static PrometheusType forMetric(MetricData metric) {
    switch (metric.getType()) {
      case LONG_GAUGE:
      case DOUBLE_GAUGE:
        return GAUGE;
      case LONG_SUM:
        SumData<LongPointData> longSumData = metric.getLongSumData();
        if (longSumData.isMonotonic()) {
          return COUNTER;
        }
        return GAUGE;
      case DOUBLE_SUM:
        SumData<DoublePointData> doubleSumData = metric.getDoubleSumData();
        if (doubleSumData.isMonotonic()) {
          return COUNTER;
        }
        return GAUGE;
      case SUMMARY:
        return SUMMARY;
      case HISTOGRAM:
      case EXPONENTIAL_HISTOGRAM:
        return HISTOGRAM;
    }
    throw new IllegalArgumentException(
        "Unsupported metric type, this generally indicates version misalignment "
            + "among opentelemetry dependencies. Please make sure to use opentelemetry-bom.");
  }

  String getTypeString() {
    return typeString;
  }
}
