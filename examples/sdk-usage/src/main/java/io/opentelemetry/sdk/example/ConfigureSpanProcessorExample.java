/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/** This example shows how to instantiate different Span Processors. */
public class ConfigureSpanProcessorExample {

  static LoggingSpanExporter exporter = new LoggingSpanExporter();

  // Get the Tracer Provider
  static TracerSdkManagement tracerProvider = OpenTelemetrySdk.getGlobalTracerManagement();
  // Acquire a tracer
  static Tracer tracer = OpenTelemetry.getGlobalTracer("ConfigureSpanProcessorExample");

  public static void main(String[] args) {

    // Example how to configure the default SpanProcessors.
    defaultSpanProcessors();
    // After this method, the following SpanProcessors are registered:
    // - SimpleSpanProcessor
    // - BatchSpanProcessor
    // - MultiSpanProcessor <- this is a container for other SpanProcessors
    // |-- SimpleSpanProcessor
    // |-- BatchSpanProcessor

    // We generate a single Span so we can see some output on the console.
    // Since there are 4 different SpanProcessor registered, this Span is exported 4 times.
    tracer.spanBuilder("Span #1").startSpan().end();

    // When exiting, it is recommended to call the shutdown method. This method calls `shutdown` on
    // all configured SpanProcessors. This way, the configured exporters can release all resources
    // and terminate their job sending the remaining traces to their back end.
    tracerProvider.shutdown();
  }

  private static void defaultSpanProcessors() {
    // OpenTelemetry offers 3 different default span processors:
    //   - SimpleSpanProcessor
    //   - BatchSpanProcessor
    //   - MultiSpanProcessor
    // Default span processors require an exporter as parameter. In this example we use the
    // LoggingSpanExporter which prints on the console output the spans.

    // Configure the simple spans processor. This span processor exports span immediately after they
    // are ended.
    SimpleSpanProcessor simpleSpansProcessor = SimpleSpanProcessor.builder(exporter).build();
    tracerProvider.addSpanProcessor(simpleSpansProcessor);

    // Configure the batch spans processor. This span processor exports span in batches.
    BatchSpanProcessor batchSpansProcessor =
        BatchSpanProcessor.builder(exporter)
            .setExportOnlySampled(true) // send to the exporter only spans that have been sampled
            .setMaxExportBatchSize(512) // set the maximum batch size to use
            .setMaxQueueSize(2048) // set the queue size. This must be >= the export batch size
            .setExporterTimeoutMillis(
                30_000) // set the max amount of time an export can run before getting
            // interrupted
            .setScheduleDelayMillis(5000) // set time between two different exports
            .build();
    tracerProvider.addSpanProcessor(batchSpansProcessor);

    // Configure the composite span processor. A Composite SpanProcessor accepts a list of Span
    // Processors.
    SpanProcessor multiSpanProcessor =
        SpanProcessor.composite(simpleSpansProcessor, batchSpansProcessor);
    tracerProvider.addSpanProcessor(multiSpanProcessor);
  }
}
