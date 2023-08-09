/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.ManagedChannelTelemetryExporterBuilder;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcNettyShadedLogRecordExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogRecordData, ResourceLogs> {

  OtlpGrpcNettyShadedLogRecordExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated feature
  void usingGrpc() throws Exception {
    ManagedChannel channel = InProcessChannelBuilder.forName("test").build();
    try (Closeable exporter = OtlpGrpcLogRecordExporter.builder().setChannel(channel).build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(UpstreamGrpcSender.class);
    } finally {
      channel.shutdownNow();
    }
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> exporterBuilder() {
    return ManagedChannelTelemetryExporterBuilder.wrap(
        TelemetryExporterBuilder.wrap(OtlpGrpcLogRecordExporter.builder()));
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> toBuilder(
      TelemetryExporter<LogRecordData> exporter) {
    return TelemetryExporterBuilder.wrap(
        ((OtlpGrpcLogRecordExporter) exporter.unwrap()).toBuilder());
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
