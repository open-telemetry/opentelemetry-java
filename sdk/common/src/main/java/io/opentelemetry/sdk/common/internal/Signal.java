/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.util.Locale;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public enum Signal {
  SPAN(
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_DESCRIPTION,
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_DESCRIPTION),
  METRIC(
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_DESCRIPTION,
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_DESCRIPTION),
  LOG(
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_DESCRIPTION,
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_NAME,
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_UNIT,
      SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_DESCRIPTION),
  PROFILE("TBD", "TBD", "TBD", "TBD", "TBD", "TBD");

  private final String exporterInflightMetricName;
  private final String exporterInflightMetricUnit;
  private final String exporterInflightMetricDescription;
  private final String exporterExportedMetricName;
  private final String exporterExportedMetricUnit;
  private final String exporterExportedMetricDescription;

  Signal(
      String exporterInflightMetricName,
      String exporterInflightMetricUnit,
      String exporterInflightMetricDescription,
      String exporterExportedMetricName,
      String exporterExportedMetricUnit,
      String exporterExportedMetricDescription) {
    this.exporterInflightMetricName = exporterInflightMetricName;
    this.exporterInflightMetricUnit = exporterInflightMetricUnit;
    this.exporterInflightMetricDescription = exporterInflightMetricDescription;
    this.exporterExportedMetricName = exporterExportedMetricName;
    this.exporterExportedMetricUnit = exporterExportedMetricUnit;
    this.exporterExportedMetricDescription = exporterExportedMetricDescription;
  }

  public String logFriendlyName() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  public String getExporterInflightMetricName() {
    return exporterInflightMetricName;
  }

  public String getExporterInflightMetricUnit() {
    return exporterInflightMetricUnit;
  }

  public String getExporterInflightMetricDescription() {
    return exporterInflightMetricDescription;
  }

  public String getExporterExportedMetricName() {
    return exporterExportedMetricName;
  }

  public String getExporterExportedMetricUnit() {
    return exporterExportedMetricUnit;
  }

  public String getExporterExportedMetricDescription() {
    return exporterExportedMetricDescription;
  }
}
