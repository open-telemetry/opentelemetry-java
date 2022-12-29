/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import java.io.Closeable;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpGrpcMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics> {

  OtlpGrpcMetricExporterTest() {
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

  /** Test configuration specific to metric exporter. */
  @Test
  void validMetricConfig() {
    assertThatCode(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setAggregationTemporalitySelector(
                        AggregationTemporalitySelector.deltaPreferred()))
        .doesNotThrowAnyException();
    assertThat(
            OtlpGrpcMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            OtlpGrpcMetricExporter.builder()
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    assertThat(
            OtlpGrpcMetricExporter.builder()
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
            () -> OtlpGrpcMetricExporter.builder().setAggregationTemporalitySelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("aggregationTemporalitySelector");

    assertThatThrownBy(() -> OtlpGrpcMetricExporter.builder().setDefaultAggregationSelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("defaultAggregationSelector");
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcMetricExporter.builder().build()) {
      assertThat(exporter).extracting("delegate").isInstanceOf(OkHttpGrpcExporter.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcMetricExporter.builder());
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
