/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.HttpLogRecordExporterBuilderWrapper;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.List;

class OtlpHttpLogRecordExporterJdkSenderTest
    extends AbstractHttpTelemetryExporterTest<LogRecordData, ResourceLogs> {

  protected OtlpHttpLogRecordExporterJdkSenderTest() {
    super("log", "/v1/logs", ResourceLogs.getDefaultInstance());
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> exporterBuilder() {
    return new HttpLogRecordExporterBuilderWrapper(OtlpHttpLogRecordExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> toBuilder(
      TelemetryExporter<LogRecordData> exporter) {
    return new HttpLogRecordExporterBuilderWrapper(
        ((OtlpHttpLogRecordExporter) exporter.unwrap()).toBuilder());
  }

  @Override
  protected LogRecordData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeLogRecordData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogRecordData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }
}
