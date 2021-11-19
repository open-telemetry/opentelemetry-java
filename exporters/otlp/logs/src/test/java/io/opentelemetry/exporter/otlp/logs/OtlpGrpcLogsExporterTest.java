/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

class OtlpGrpcLogsExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogData, ResourceLogs, OtlpGrpcLogExporter> {

  OtlpGrpcLogsExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Override
  protected OtlpGrpcLogExporter createExporter(String endpoint) {
    return OtlpGrpcLogExporter.builder().setEndpoint(endpoint).build();
  }

  @Override
  protected OtlpGrpcLogExporter createExporterWithTimeout(String endpoint, Duration timeout) {
    return OtlpGrpcLogExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
  }

  @Override
  protected CompletableResultCode shutdownExporter(OtlpGrpcLogExporter exporter) {
    return exporter.shutdown();
  }

  @Override
  protected CompletableResultCode doExport(OtlpGrpcLogExporter exporter, List<LogData> telemetry) {
    return exporter.export(telemetry);
  }

  @Override
  protected LogData generateFakeTelemetry() {
    return LogDataBuilder.create(
            Resource.create(Attributes.builder().put("testKey", "testValue").build()),
            InstrumentationLibraryInfo.create("instrumentation", "1"))
        .setEpoch(Instant.now())
        .setSeverity(Severity.ERROR)
        .setSeverityText("really severe")
        .setName("log1")
        .setBody("message")
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }
}
