/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.exporter.internal.grpc.UpstreamGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.ManagedChannelTelemetryExporterBuilder;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcNettyMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics> {

  OtlpGrpcNettyMetricExporterTest() {
    super("metric", ResourceMetrics.getDefaultInstance());
  }

  @Test
  void testSetRetryPolicyOnDelegate() {
    assertThatCode(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    OtlpGrpcMetricExporter.builder(), RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated feature
  void usingGrpc() throws Exception {
    ManagedChannel channel = InProcessChannelBuilder.forName("test").build();
    try (Closeable exporter = OtlpGrpcMetricExporter.builder().setChannel(channel).build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(UpstreamGrpcExporter.class);
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
  protected MetricData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeMetricData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<MetricData> telemetry) {
    return ResourceMetricsMarshaler.create(telemetry);
  }
}
