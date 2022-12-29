/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpHttpMetricExporterTest
    extends AbstractHttpTelemetryExporterTest<MetricData, ResourceMetrics> {

  protected OtlpHttpMetricExporterTest() {
    super("metric", "/v1/metrics", ResourceMetrics.getDefaultInstance());
  }

  /** Test configuration specific to metric exporter. */
  @Test
  void validMetricConfig() {
    assertThatCode(
            () ->
                OtlpHttpMetricExporter.builder()
                    .setAggregationTemporalitySelector(
                        AggregationTemporalitySelector.deltaPreferred()))
        .doesNotThrowAnyException();
    assertThat(
            OtlpHttpMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            OtlpHttpMetricExporter.builder()
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    assertThat(
            OtlpHttpMetricExporter.builder()
                .setDefaultAggregationSelector(
                    DefaultAggregationSelector.getDefault()
                        .with(InstrumentType.HISTOGRAM, Aggregation.drop()))
                .build()
                .getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.drop());
  }

  /** Test configuration specific to metric exporter. */
  @Test
  void invalidMetricConfig() {
    assertThatThrownBy(
            () -> OtlpHttpMetricExporter.builder().setAggregationTemporalitySelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("aggregationTemporalitySelector");

    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setDefaultAggregationSelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("defaultAggregationSelector");
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder();
    return new TelemetryExporterBuilder<MetricData>() {
      @Override
      public TelemetryExporterBuilder<MetricData> setEndpoint(String endpoint) {
        builder.setEndpoint(endpoint);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setTimeout(long timeout, TimeUnit unit) {
        builder.setTimeout(timeout, unit);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setTimeout(Duration timeout) {
        builder.setTimeout(timeout);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setCompression(String compression) {
        builder.setCompression(compression);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> addHeader(String key, String value) {
        builder.addHeader(key, value);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setTrustedCertificates(byte[] certificates) {
        builder.setTrustedCertificates(certificates);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setClientTls(
          byte[] privateKeyPem, byte[] certificatePem) {
        builder.setClientTls(privateKeyPem, certificatePem);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setRetryPolicy(RetryPolicy retryPolicy) {
        RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<MetricData> setChannel(io.grpc.ManagedChannel channel) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public TelemetryExporter<MetricData> build() {
        return TelemetryExporter.wrap(builder.build());
      }
    };
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
