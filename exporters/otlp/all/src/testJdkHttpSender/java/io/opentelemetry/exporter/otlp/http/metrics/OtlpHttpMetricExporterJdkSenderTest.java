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
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.HttpMetricExporterBuilderWrapper;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSender;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import java.util.List;
import org.junit.jupiter.api.Test;

class OtlpHttpMetricExporterJdkSenderTest
    extends AbstractHttpTelemetryExporterTest<MetricData, ResourceMetrics> {

  protected OtlpHttpMetricExporterJdkSenderTest() {
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
  protected boolean hasAuthenticatorSupport() {
    return false;
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> exporterBuilder() {
    return new HttpMetricExporterBuilderWrapper(OtlpHttpMetricExporter.builder());
  }

  @Override
  protected TelemetryExporterBuilder<MetricData> toBuilder(TelemetryExporter<MetricData> exporter) {
    return new HttpMetricExporterBuilderWrapper(
        ((OtlpHttpMetricExporter) exporter.unwrap()).toBuilder());
  }

  @Test
  void isJdkHttpSender() {
    TelemetryExporter<MetricData> exporter = exporterBuilder().build();

    try {
      assertThat(exporter.unwrap())
          .extracting("delegate")
          .extracting("httpSender")
          .isInstanceOf(JdkHttpSender.class);
    } finally {
      exporter.shutdown();
    }
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
