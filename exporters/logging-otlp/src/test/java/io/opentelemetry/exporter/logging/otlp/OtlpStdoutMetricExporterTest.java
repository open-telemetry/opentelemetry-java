/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class OtlpStdoutMetricExporterTest
    extends AbstractOtlpStdoutExporterTest<OtlpStdoutMetricExporter> {

  public OtlpStdoutMetricExporterTest() {
    super(
        TestDataExporter.forMetrics(),
        OtlpStdoutMetricExporter.class,
        ConfigurableMetricExporterProvider.class,
        MetricExporter.class,
        "OtlpStdoutMetricExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true}");
  }

  @Override
  protected OtlpStdoutMetricExporter createDefaultExporter() {
    return OtlpStdoutMetricExporter.builder().build();
  }

  @Override
  protected OtlpStdoutMetricExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutMetricExporterBuilder builder =
        OtlpStdoutMetricExporter.builder().setWrapperJsonObject(wrapperJsonObject);
    if (outputStream != null) {
      builder.setOutput(outputStream);
    } else {
      builder.setOutput(Logger.getLogger(exporterClass.getName()));
    }
    return builder.build();
  }

  /** Test configuration specific to metric exporter. */
  @Test
  void providerMetricConfig() {
    OtlpStdoutMetricExporter exporter =
        loadExporter(
            DefaultConfigProperties.createFromMap(
                ImmutableMap.of(
                    "otel.exporter.otlp.metrics.temporality.preference",
                    "DELTA",
                    "otel.exporter.otlp.metrics.default.histogram.aggregation",
                    "BASE2_EXPONENTIAL_BUCKET_HISTOGRAM")));

    assertThat(exporter.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);

    assertThat(exporter.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
  }

  @Test
  void componentProviderMetricConfig() {
    StructuredConfigProperties properties = mock(StructuredConfigProperties.class);
    when(properties.getString("temporality_preference")).thenReturn("DELTA");
    when(properties.getString("default_histogram_aggregation"))
        .thenReturn("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");

    OtlpStdoutMetricExporter exporter =
        (OtlpStdoutMetricExporter) exporterFromComponentProvider(properties);
    assertThat(exporter.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);

    assertThat(exporter.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
  }

  @Test
  void validMetricConfig() {
    assertThatCode(
            () ->
                OtlpStdoutMetricExporter.builder()
                    .setAggregationTemporalitySelector(
                        AggregationTemporalitySelector.deltaPreferred()))
        .doesNotThrowAnyException();
    assertThat(
            OtlpStdoutMetricExporter.builder()
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            OtlpStdoutMetricExporter.builder()
                .build()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    assertThat(
            OtlpStdoutMetricExporter.builder()
                .setDefaultAggregationSelector(
                    DefaultAggregationSelector.getDefault()
                        .with(InstrumentType.HISTOGRAM, Aggregation.drop()))
                .build()
                .getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.drop());
  }
}
