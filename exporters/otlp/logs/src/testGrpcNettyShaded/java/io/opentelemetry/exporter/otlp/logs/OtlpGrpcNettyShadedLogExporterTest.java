/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.grpc.UpstreamGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.ManagedChannelTelemetryExporterBuilder;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogData;
import java.io.Closeable;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcNettyShadedLogExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogData, ResourceLogs> {

  OtlpGrpcNettyShadedLogExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated feature
  void usingGrpc() throws Exception {
    try (Closeable exporter =
        OtlpGrpcLogExporter.builder()
            .setChannel(InProcessChannelBuilder.forName("test").build())
            .build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(UpstreamGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<LogData> exporterBuilder() {
    return ManagedChannelTelemetryExporterBuilder.wrap(
        TelemetryExporterBuilder.wrap(OtlpGrpcLogExporter.builder()));
  }

  @Override
  protected LogData generateFakeTelemetry() {
    return TestLogData.builder()
        .setResource(Resource.create(Attributes.builder().put("testKey", "testValue").build()))
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.create("instrumentation", "1", null))
        .setEpoch(Instant.now())
        .setSeverity(Severity.ERROR)
        .setSeverityText("really severe")
        .setBody("message")
        .setAttributes(Attributes.builder().put("animal", "cat").build())
        .build();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }
}
