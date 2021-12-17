/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.otlp.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics> {

  OtlpGrpcMetricExporterTest() {
    super("metric", ResourceMetrics.getDefaultInstance());
  }

  @Test
  void usingOkHttp() {
    assertThat(OtlpGrpcMetricExporter.builder().delegate)
        .isInstanceOf(OkHttpGrpcExporterBuilder.class);
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder();
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
      public TelemetryExporterBuilder<MetricData> setRetryPolicy(RetryPolicy retryPolicy) {
        builder.delegate.setRetryPolicy(retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporter<MetricData> build() {
        return TelemetryExporter.wrap(builder.build());
      }
    };
  }

  @Override
  protected MetricData generateFakeTelemetry() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricData.createLongSum(
        Resource.empty(),
        InstrumentationLibraryInfo.empty(),
        "name",
        "description",
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  @Override
  protected Marshaler[] toMarshalers(List<MetricData> telemetry) {
    return ResourceMetricsMarshaler.create(telemetry);
  }
}
