package io.opentelemetry.example.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.Collections;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
public class ExampleConfiguration {

  /** The number of milliseconds between metric exports. */
  private static final long METRIC_EXPORT_INTERVAL_MS = 800L;

  /**
   * Initializes an OpenTelemetry SDK with a logging exporter and a SimpleSpanProcessor.
   *
   * @return A ready-to-use {@link OpenTelemetry} instance.
   */
  public static void initOpenTelemetry() {
    // This will be used to create instruments
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();

    // Create an instance of IntervalMetricReader and configure it
    // to read metrics from the meterProvider and export them to the logging exporter
    IntervalMetricReader.builder()
        .setMetricExporter(new LoggingMetricExporter())
        .setMetricProducers(Collections.singleton(meterProvider))
        .setExportIntervalMillis(METRIC_EXPORT_INTERVAL_MS)
        .build();
    // Tracer provider configured to export spans with SimpleSpanProcessor using
    // the logging exporter.
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
            .build();
    OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();
  }
}
