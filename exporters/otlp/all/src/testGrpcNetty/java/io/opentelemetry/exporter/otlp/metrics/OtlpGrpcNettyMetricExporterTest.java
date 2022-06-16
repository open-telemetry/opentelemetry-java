/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.grpc.DefaultGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.ManagedChannelTelemetryExporterBuilder;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    try (Closeable exporter =
        OtlpGrpcMetricExporter.builder()
            .setChannel(InProcessChannelBuilder.forName("test").build())
            .build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(DefaultGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    return ManagedChannelTelemetryExporterBuilder.wrap(
        TelemetryExporterBuilder.wrap(OtlpGrpcMetricExporter.builder()));
  }

  @Override
  protected MetricData generateFakeTelemetry() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return ImmutableMetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "name",
        "description",
        "1",
        ImmutableSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableLongPointData.create(
                    startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  @Override
  protected Marshaler[] toMarshalers(List<MetricData> telemetry) {
    return ResourceMetricsMarshaler.create(telemetry);
  }
}
