/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.ManagedChannelTelemetryExporterBuilder;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.grpc.managedchannel.internal.UpstreamGrpcSender;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcOkHttpMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics> {

  OtlpGrpcOkHttpMetricExporterTest() {
    super("metric", ResourceMetrics.getDefaultInstance());
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated featurea
  void usingGrpc() throws Exception {
    ManagedChannel channel = InProcessChannelBuilder.forName("test").build();
    try (Closeable exporter = OtlpGrpcMetricExporter.builder().setChannel(channel).build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(UpstreamGrpcSender.class);
    } finally {
      channel.shutdownNow();
    }
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    return ManagedChannelTelemetryExporterBuilder.wrap(
        TelemetryExporterBuilder.wrap(OtlpGrpcMetricExporter.builder()));
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> toBuilder(TelemetryExporter<MetricData> exporter) {
    return TelemetryExporterBuilder.wrap(((OtlpGrpcMetricExporter) exporter.unwrap()).toBuilder());
  }

  @Override
  protected MetricData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeMetricData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<MetricData> telemetry) {
    return ResourceMetricsMarshaler.create(telemetry);
  }
}
