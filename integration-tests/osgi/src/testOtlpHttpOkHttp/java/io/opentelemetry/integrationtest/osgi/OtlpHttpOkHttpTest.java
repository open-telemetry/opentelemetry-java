/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;

/**
 * Verifies OTLP HTTP exporters with OkHttp sender work in OSGi.
 *
 * <p>See {@code extraRunsystempackages} in {@code build.gradle.kts} for the system packages that
 * must be exposed in the OSGi container for this sender to function.
 */
@ExtendWith(BundleContextExtension.class)
public class OtlpHttpOkHttpTest {

  @Test
  void sdkWithOtlpHttpExportersInitializes() {
    try (OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(
                        SimpleSpanProcessor.create(OtlpHttpSpanExporter.builder().build()))
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(
                        PeriodicMetricReader.create(OtlpHttpMetricExporter.builder().build()))
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(
                        SimpleLogRecordProcessor.create(
                            OtlpHttpLogRecordExporter.builder().build()))
                    .build())
            .build()) {
      assertThat(sdk).isNotNull();
    }
  }
}
