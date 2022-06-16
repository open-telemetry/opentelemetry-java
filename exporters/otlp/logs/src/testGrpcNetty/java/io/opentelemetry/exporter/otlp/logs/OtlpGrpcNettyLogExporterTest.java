/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.grpc.DefaultGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcNettyLogExporterTest
    extends AbstractGrpcTelemetryExporterTest<LogData, ResourceLogs> {

  OtlpGrpcNettyLogExporterTest() {
    super("log", ResourceLogs.getDefaultInstance());
  }

  @Test
  void testSetRetryPolicyOnDelegate() {
    assertThatCode(
            () ->
                RetryUtil.setRetryPolicyOnDelegate(
                    OtlpGrpcLogExporter.builder(), RetryPolicy.getDefault()))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("deprecation") // testing deprecated feature
  void usingGrpc() throws Exception {
    try (Closeable exporter =
        OtlpGrpcLogExporter.builder()
            .setChannel(InProcessChannelBuilder.forName("test").build())
            .build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(DefaultGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<LogData> exporterBuilder() {
    OtlpGrpcLogExporterBuilder builder = OtlpGrpcLogExporter.builder();
    return new TelemetryExporterBuilder<LogData>() {
      private ManagedChannel channel;

      @Override
      @SuppressWarnings("deprecation") // testing deprecated feature
      public TelemetryExporterBuilder<LogData> setEndpoint(String endpoint) {
        URI uri = URI.create(endpoint);
        channel = ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort()).build();
        builder.setChannel(channel);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setTimeout(long timeout, TimeUnit unit) {
        builder.setTimeout(timeout, unit);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setTimeout(Duration timeout) {
        builder.setTimeout(timeout);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setCompression(String compression) {
        builder.setCompression(compression);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> addHeader(String key, String value) {
        builder.addHeader(key, value);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setTrustedCertificates(byte[] certificates) {
        builder.setTrustedCertificates(certificates);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setClientTls(
          byte[] privateKeyPem, byte[] certificatePem) {
        builder.setClientTls(privateKeyPem, certificatePem);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogData> setRetryPolicy(RetryPolicy retryPolicy) {
        builder.delegate.setRetryPolicy(retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporter<LogData> build() {
        return TelemetryExporter.wrap(builder.build(), channel::shutdownNow);
      }
    };
  }

  @Override
  protected LogData generateFakeTelemetry() {
    return LogDataBuilder.create(
            Resource.create(Attributes.builder().put("testKey", "testValue").build()),
            InstrumentationScopeInfo.create("instrumentation", "1", null))
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
