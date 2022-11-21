/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.collect.ImmutableMap;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LoggingOtlpTest {

  @RegisterExtension
  LogCapturer spansCapturer =
      LogCapturer.create().captureForType(OtlpJsonLoggingSpanExporter.class);

  @RegisterExtension
  LogCapturer metricsCapturer =
      LogCapturer.create().captureForType(OtlpJsonLoggingMetricExporter.class);

  @RegisterExtension
  LogCapturer logsCapturer =
      LogCapturer.create().captureForType(OtlpJsonLoggingLogRecordExporter.class);

  @Test
  void configures() {
    OpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setConfig(
                DefaultConfigProperties.createForTest(
                    ImmutableMap.of(
                        "otel.traces.exporter", "logging-otlp",
                        "otel.metrics.exporter", "logging-otlp",
                        "otel.logs.exporter", "logging-otlp")))
            .setResultAsGlobal(false)
            .build()
            .getOpenTelemetrySdk();

    sdk.getTracerProvider().get("tracer").spanBuilder("test").startSpan().end();
    sdk.getMeterProvider().get("meter").counterBuilder("counter").build().add(10);
    sdk.getSdkLoggerProvider().get("logger").logRecordBuilder().setBody("message").emit();

    sdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    sdk.getSdkMeterProvider().forceFlush().join(10, TimeUnit.SECONDS);
    sdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);

    await().untilAsserted(() -> assertThat(spansCapturer.getEvents().size()).isGreaterThanOrEqualTo(1));
    await().untilAsserted(() -> assertThat(metricsCapturer.getEvents().size()).isGreaterThanOrEqualTo(1));
    await().untilAsserted(() -> assertThat(logsCapturer.getEvents().size()).isGreaterThanOrEqualTo(1));
  }
}
