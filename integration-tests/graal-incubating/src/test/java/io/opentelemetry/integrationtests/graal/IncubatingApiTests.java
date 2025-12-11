/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class IncubatingApiTests {
  @Test
  void incubatingApiIsLoadedViaReflection() {
    assertThat(LoggerProvider.noop().get("test")).isInstanceOf(ExtendedLogger.class);
    assertThat(TracerProvider.noop().get("test")).isInstanceOf(ExtendedTracer.class);
    assertThat(MeterProvider.noop().get("test").counterBuilder("test"))
        .isInstanceOf(ExtendedLongCounterBuilder.class);
  }

  @Test
  void incubatingLogSdk() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();

    ExtendedLogger logger = (ExtendedLogger) loggerProvider.get("logger");
    logger.isEnabled(Severity.INFO);
    logger.logRecordBuilder().setSeverity(Severity.INFO).setBody("message").emit();
  }

  @Test
  void incubatingTraceSdk() {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

    ExtendedTracer tracer = (ExtendedTracer) tracerProvider.get("tracer");
    tracer.isEnabled();
    tracer.spanBuilder("span").startAndRun(() -> {});
  }

  @Test
  void incubatingMetricSdk() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build();

    Meter meter = meterProvider.get("meter");

    ExtendedLongCounter longCounter =
        (ExtendedLongCounter) meter.counterBuilder("longCounter").build();
    longCounter.isEnabled();
    ExtendedDoubleCounter doubleCounter =
        (ExtendedDoubleCounter) meter.counterBuilder("doubleCounter").ofDoubles().build();
    doubleCounter.isEnabled();
    ExtendedLongUpDownCounter longUpDownCounter =
        (ExtendedLongUpDownCounter) meter.upDownCounterBuilder("longUpDownCounter").build();
    longUpDownCounter.isEnabled();
    ExtendedDoubleUpDownCounter doubleUpDownCounter =
        (ExtendedDoubleUpDownCounter)
            meter.upDownCounterBuilder("doubleUpDownCounter").ofDoubles().build();
    doubleUpDownCounter.isEnabled();
    ExtendedDoubleHistogram doubleHistogram =
        (ExtendedDoubleHistogram) meter.histogramBuilder("doubleHistogram").build();
    doubleHistogram.isEnabled();
    ExtendedLongHistogram longHistogram =
        (ExtendedLongHistogram) meter.histogramBuilder("longHistogram").ofLongs().build();
    longHistogram.isEnabled();
    ExtendedDoubleGauge doubleGauge =
        (ExtendedDoubleGauge) meter.gaugeBuilder("doubleGauge").build();
    doubleGauge.isEnabled();
    ExtendedLongGauge longGauge =
        (ExtendedLongGauge) meter.gaugeBuilder("longGauge").ofLongs().build();
    longGauge.isEnabled();
  }

  @Test
  void parseDeclarativeConfiguration() {
    // make sure to test enums too: "instrument_type: histogram"
    String string =
        """
      file_format: "1.0-rc.1"
      tracer_provider:
        processors:
          - batch:
              exporter:
                console: {}
      meter_provider:
        views:
          - selector:
              instrument_type: histogram
            stream:
              aggregation:
                drop: {}
      """;
    // should not throw
    DeclarativeConfiguration.parse(
        new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
  }
}
