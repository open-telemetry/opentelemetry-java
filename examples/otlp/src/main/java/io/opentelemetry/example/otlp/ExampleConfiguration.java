/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.otlp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
public final class ExampleConfiguration {

  /**
   * Adds a BatchSpanProcessor initialized with OtlpGrpcSpanExporter to the TracerSdkProvider.
   *
   * @return a ready-to-use {@link OpenTelemetry} instance.
   */
  static OpenTelemetry initOpenTelemetry() {
    OtlpGrpcSpanExporter spanExporter =
        OtlpGrpcSpanExporter.builder().setTimeout(2, TimeUnit.SECONDS).build();
    BatchSpanProcessor spanProcessor =
        BatchSpanProcessor.builder(spanExporter)
            .setScheduleDelay(100, TimeUnit.MILLISECONDS)
            .build();

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(AutoConfiguredOpenTelemetrySdk.initialize().getResource())
            .build();
    OpenTelemetrySdk openTelemetrySdk =
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

    Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::shutdown));

    return openTelemetrySdk;
  }

  /**
   * Initializes a Metrics SDK with a OtlpGrpcMetricExporter and an IntervalMetricReader.
   *
   * @return a ready-to-use {@link MeterProvider} instance
   */
  static MeterProvider initOpenTelemetryMetrics() {
    // set up the metric exporter and wire it into the SDK and a timed periodic reader.
    OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.getDefault();

    MetricReaderFactory periodicReaderFactory =
        PeriodicMetricReader.builder(metricExporter)
            .setInterval(Duration.ofMillis(1000))
            .newMetricReaderFactory();

    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(periodicReaderFactory)
            .buildAndRegisterGlobal();

    Runtime.getRuntime().addShutdownHook(new Thread(sdkMeterProvider::shutdown));
    return sdkMeterProvider;
  }
}
