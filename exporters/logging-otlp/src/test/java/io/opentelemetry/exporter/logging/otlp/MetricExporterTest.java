/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpJsonLoggingMetricExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import java.io.OutputStream;
import java.util.Arrays;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

@SuppressLogger(OtlpJsonLoggingMetricExporter.class)
class MetricExporterTest
    extends AbstractOtlpJsonLoggingExporterTest<OtlpJsonLoggingMetricExporter> {

  private static final MetricData METRIC1 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation")
              .setVersion("1")
              .setAttributes(Attributes.builder().put("key", "value").build())
              .build(),
          "metric1",
          "metric1 description",
          "m",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  private static final MetricData METRIC2 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build(),
          "metric2",
          "metric2 description",
          "s",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  public MetricExporterTest() {
    super(
        OtlpJsonLoggingMetricExporter.class,
        ConfigurableMetricExporterProvider.class,
        MetricExporter.class,
        "expected-metrics.json",
        "expected-metrics-wrapper.json",
        "OtlpJsonLoggingMetricExporter{memoryMode=IMMUTABLE_DATA, wrapperJsonObject=false, jsonWriter=LoggerJsonWriter, aggregationTemporalitySelector=AggregationTemporalitySelector{COUNTER=CUMULATIVE, UP_DOWN_COUNTER=CUMULATIVE, HISTOGRAM=CUMULATIVE, OBSERVABLE_COUNTER=CUMULATIVE, OBSERVABLE_UP_DOWN_COUNTER=CUMULATIVE, OBSERVABLE_GAUGE=CUMULATIVE, GAUGE=CUMULATIVE}, defaultAggregationSelector=DefaultAggregationSelector{COUNTER=default, UP_DOWN_COUNTER=default, HISTOGRAM=default, OBSERVABLE_COUNTER=default, OBSERVABLE_UP_DOWN_COUNTER=default, OBSERVABLE_GAUGE=default, GAUGE=default}}");
  }

  @Override
  protected OtlpJsonLoggingMetricExporter createExporter(
      @Nullable OutputStream outputStream, MemoryMode memoryMode, boolean wrapperJsonObject) {
    OtlpJsonLoggingMetricExporterBuilder builder = OtlpStdoutMetricExporter.builder();
    if (outputStream == null) {
      builder.setUseLogger();
    } else {
      builder.setOutputStream(outputStream);
    }

    return builder.setMemoryMode(memoryMode).setWrapperJsonObject(wrapperJsonObject).build();
  }

  @Override
  protected OtlpJsonLoggingMetricExporter createDefaultExporter() {
    return (OtlpJsonLoggingMetricExporter) OtlpJsonLoggingMetricExporter.create();
  }

  @Override
  protected OtlpJsonLoggingMetricExporter createDefaultStdoutExporter() {
    return OtlpStdoutMetricExporter.create();
  }

  @Override
  protected OtlpJsonLoggingMetricExporter toBuilderAndBack(OtlpJsonLoggingMetricExporter exporter) {
    return OtlpJsonLoggingMetricExporterBuilder.createFromExporter(exporter).build();
  }

  @Override
  protected CompletableResultCode export(OtlpJsonLoggingMetricExporter exporter) {
    return exporter.export(Arrays.asList(METRIC1, METRIC2));
  }

  @Override
  protected CompletableResultCode flush(OtlpJsonLoggingMetricExporter exporter) {
    return exporter.flush();
  }

  @Override
  protected CompletableResultCode shutdown(OtlpJsonLoggingMetricExporter exporter) {
    return exporter.shutdown();
  }

  @Test
  void providerConfig() {
    OtlpJsonLoggingMetricExporter exporter =
        loadExporter(
            DefaultConfigProperties.createFromMap(
                ImmutableMap.of(
                    "otel.exporter.otlp.metrics.temporality.preference",
                    "DELTA",
                    "otel.exporter.otlp.metrics.default.histogram.aggregation",
                    "BASE2_EXPONENTIAL_BUCKET_HISTOGRAM")),
            "otlp-stdout");

    assertThat(exporter.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);

    assertThat(exporter.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
  }

  @Test
  void componentProviderConfig() {
    StructuredConfigProperties properties = mock(StructuredConfigProperties.class);
    when(properties.getString("temporality_preference")).thenReturn("DELTA");
    when(properties.getString("default_histogram_aggregation"))
        .thenReturn("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");

    OtlpJsonLoggingMetricExporter exporter = exporterFromComponentProvider(properties);
    assertThat(exporter.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);

    assertThat(exporter.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
  }

  /** Test configuration specific to metric exporter. */
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
