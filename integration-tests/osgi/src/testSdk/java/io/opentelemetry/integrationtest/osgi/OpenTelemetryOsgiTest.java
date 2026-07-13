/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;

/** Verifies the SDK (traces, metrics, logs) and Context API work in OSGi. */
@ExtendWith(BundleContextExtension.class)
public class OpenTelemetryOsgiTest {

  @Test
  public void verifyContextStorage() {
    ContextKey<String> testContextKey = ContextKey.named("test");
    try (Scope scope = Context.current().with(testContextKey, "testValue").makeCurrent()) {
      assertThat(Context.current().get(testContextKey)).isEqualTo("testValue");
    }
  }

  @Test
  public void vanillaSdkInitializes() {
    try (OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(
                        PeriodicMetricReader.create(
                            new MetricExporter() {
                              @Override
                              public CompletableResultCode export(Collection<MetricData> metrics) {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode flush() {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode shutdown() {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public AggregationTemporality getAggregationTemporality(
                                  InstrumentType instrumentType) {
                                return AggregationTemporality.CUMULATIVE;
                              }
                            }))
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(
                        SimpleLogRecordProcessor.create(
                            new LogRecordExporter() {
                              @Override
                              public CompletableResultCode export(Collection<LogRecordData> logs) {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode flush() {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode shutdown() {
                                return CompletableResultCode.ofSuccess();
                              }
                            }))
                    .build())
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(
                        SimpleSpanProcessor.create(
                            new SpanExporter() {
                              @Override
                              public CompletableResultCode export(Collection<SpanData> spans) {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode flush() {
                                return CompletableResultCode.ofSuccess();
                              }

                              @Override
                              public CompletableResultCode shutdown() {
                                return CompletableResultCode.ofSuccess();
                              }
                            }))
                    .build())
            .build()) {
      assertThat(sdk).isNotNull();

      // Verify Context API is available
      Context current = Context.current();
      assertThat(current).isNotNull();
    }
  }
}
