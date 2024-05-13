/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.junit.jupiter.api.Test;

class InitializeSdkTest {

  @Test
  void initializeSdk() {
    assertThatCode(
            () -> {
              OpenTelemetrySdk sdk =
                  OpenTelemetrySdk.builder()
                      .setTracerProvider(
                          SdkTracerProvider.builder()
                              .addSpanProcessor(
                                  BatchSpanProcessor.builder(OtlpGrpcSpanExporter.getDefault())
                                      .build())
                              .build())
                      .setMeterProvider(
                          SdkMeterProvider.builder()
                              .registerMetricReader(
                                  PeriodicMetricReader.create(OtlpGrpcMetricExporter.getDefault()))
                              .build())
                      .setLoggerProvider(
                          SdkLoggerProvider.builder()
                              .addLogRecordProcessor(
                                  BatchLogRecordProcessor.builder(
                                          OtlpGrpcLogRecordExporter.getDefault())
                                      .build())
                              .build())
                      .build();
              sdk.close();
            })
        .doesNotThrowAnyException();
  }
}
