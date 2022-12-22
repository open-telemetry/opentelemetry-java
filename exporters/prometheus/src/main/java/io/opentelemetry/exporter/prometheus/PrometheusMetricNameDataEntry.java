/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class PrometheusMetricNameDataEntry {

  public static PrometheusMetricNameDataEntry create(String metricName, MetricData metricData) {
    return new AutoValue_PrometheusMetricNameDataEntry(metricName, metricData);
  }

  public abstract String getMetricName();

  public abstract MetricData getMetricData();
}
