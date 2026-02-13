/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractGrpcTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.okhttp4.internal.OkHttpGrpcSender;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OtlpGrpcMetricExporterTest
    extends AbstractGrpcTelemetryExporterTest<MetricData, ResourceMetrics> {

  OtlpGrpcMetricExporterTest() {
    super("metric", ResourceMetrics.getDefaultInstance());
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

  /** Test configuration specific to metric exporter. */
  @Test
  void stringRepresentation() {
    try (MetricExporter metricExporter = OtlpGrpcMetricExporter.builder().build()) {
      assertThat(metricExporter.toString())
          .matches(
              "OtlpGrpcMetricExporter\\{"
                  + "endpoint=http://localhost:4317, "
                  + "fullMethodName=.*, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "compressorEncoding=null, "
                  + "headers=Headers\\{User-Agent=OBFUSCATED\\}, "
                  + "retryPolicy=RetryPolicy\\{.*\\}, "
                  + "componentLoader=.*, "
                  + "exporterType=OTLP_GRPC_METRIC_EXPORTER, "
                  + "internalTelemetrySchemaVersion=LEGACY, "
                  + "aggregationTemporalitySelector=AggregationTemporalitySelector\\{.*\\}, "
                  + "defaultAggregationSelector=DefaultAggregationSelector\\{.*\\}, "
                  + "memoryMode=REUSABLE_DATA"
                  + "\\}");
    }
  }

  @Test
  void usingOkHttp() throws Exception {
    try (Closeable exporter = OtlpGrpcMetricExporter.builder().build()) {
      assertThat(exporter).extracting("delegate.grpcSender").isInstanceOf(OkHttpGrpcSender.class);
    }
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    return TelemetryExporterBuilder.wrap(OtlpGrpcMetricExporter.builder());
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
