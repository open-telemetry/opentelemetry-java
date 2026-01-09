/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import java.util.Locale;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public enum Signal {
  SPAN("otel.sdk.exporter.span", "span"),
  METRIC("otel.sdk.exporter.metric_data_point", "data_point"),
  LOG("otel.sdk.exporter.log", "log_record"),
  PROFILE("TBD", "TBD");

  private final String exporterMetricNamespace;
  private final String metricUnit;

  Signal(String exporterMetricNamespace, String metricUnit) {
    this.exporterMetricNamespace = exporterMetricNamespace;
    this.metricUnit = metricUnit;
  }

  public String logFriendlyName() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  public String getExporterMetricNamespace() {
    return exporterMetricNamespace;
  }

  public String getMetricUnit() {
    return metricUnit;
  }
}
