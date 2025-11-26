/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.common.export.MemoryMode;
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
        "OtlpStdoutMetricExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true, memoryMode=IMMUTABLE_DATA, aggregationTemporalitySelector=AggregationTemporalitySelector{COUNTER=CUMULATIVE, UP_DOWN_COUNTER=CUMULATIVE, HISTOGRAM=CUMULATIVE, OBSERVABLE_COUNTER=CUMULATIVE, OBSERVABLE_UP_DOWN_COUNTER=CUMULATIVE, OBSERVABLE_GAUGE=CUMULATIVE, GAUGE=CUMULATIVE}, defaultAggregationSelector=DefaultAggregationSelector{COUNTER=default, UP_DOWN_COUNTER=default, HISTOGRAM=default, OBSERVABLE_COUNTER=default, OBSERVABLE_UP_DOWN_COUNTER=default, OBSERVABLE_GAUGE=default, GAUGE=default}}");
  }

  @Override
  protected OtlpStdoutMetricExporter createDefaultExporter() {
    return OtlpStdoutMetricExporter.builder().build();
  }

  @Override
  protected OtlpStdoutMetricExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
    OtlpStdoutMetricExporterBuilder builder =
        OtlpStdoutMetricExporter.builder()
            .setMemoryMode(memoryMode)
            .setWrapperJsonObject(wrapperJsonObject);
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
        exporterFromProvider(
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
    DeclarativeConfigProperties properties = spy(DeclarativeConfigProperties.empty());
    when(properties.getString("temporality_preference")).thenReturn("DELTA");
    when(properties.getString("default_histogram_aggregation"))
        .thenReturn("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");

    OtlpStdoutMetricExporter exporter = exporterFromComponentProvider(properties);
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
