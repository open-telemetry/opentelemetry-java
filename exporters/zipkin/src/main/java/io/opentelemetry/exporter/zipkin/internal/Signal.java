/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

/**
 * Copied from {@code io.opentelemetry.sdk.common.internal.Signal} to avoid shared internal code.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public enum Signal {
  SPAN("otel.sdk.exporter.span", "span");

  private final String exporterMetricNamespace;
  private final String metricUnit;

  Signal(String exporterMetricNamespace, String metricUnit) {
    this.exporterMetricNamespace = exporterMetricNamespace;
    this.metricUnit = metricUnit;
  }

  public String getExporterMetricNamespace() {
    return exporterMetricNamespace;
  }

  public String getMetricUnit() {
    return metricUnit;
  }
}
