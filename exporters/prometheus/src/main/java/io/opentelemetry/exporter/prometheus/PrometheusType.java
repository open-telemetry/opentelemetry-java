/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;

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
        LongSumData longSumData = metric.getLongSumData();
        if (longSumData.isMonotonic()
            && longSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
          return COUNTER;
        }
        return GAUGE;
      case DOUBLE_SUM:
        DoubleSumData doubleSumData = metric.getDoubleSumData();
        if (doubleSumData.isMonotonic()
            && doubleSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
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
