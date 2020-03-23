/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.example;

import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.example.unsafe.DemoUtils;
import io.opentelemetry.sdk.trace.MultiSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.BatchSpansProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import java.util.Arrays;

/** This example shows how to instantiate different Span Processors. */
class ConfigureSpanProcessorExample {

  static LoggingSpanExporter exporter = new LoggingSpanExporter();

  // Get the Tracer Provider
  static TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
  // Acquire a tracer
  static TracerSdk tracer = tracerProvider.get("ConfigureSpanProcessorExample");

  public static void main(String[] args) throws Exception {

    // Example how to configure the default SpanProcessors.
    defaultSpanProcessors();
    // Print to the console the list of span processors enabled.
    DemoUtils.printProcessorList(tracer);

    // Example how to create your own SpanProcessor.
    customSpanProcessor();
    // Print to the console the list of span processors enabled.
    DemoUtils.printProcessorList(tracer);

    // We generate some Spans so we can see some output on the console.
    tracer.spanBuilder("Span #1").startSpan().end();
    tracer.spanBuilder("Span #2").startSpan().end();
    tracer.spanBuilder("Span #3").startSpan().end();

    // When exiting, it is recommended to call the shutdown method. This method calls `shutdown` on
    // all configured SpanProcessors. This way, the configured exporters can release all resources
    // and terminate their job sending the remaining traces to their back end.
    OpenTelemetrySdk.getTracerProvider().shutdown();
  }

  private static void defaultSpanProcessors() throws Exception {
    // OpenTelemetry offers 3 different default span processors:
    //   - SimpleSpanProcessor
    //   - BatchSpanProcessor
    //   - MultiSpanProcessor
    // Default span processors require an exporter as parameter. In this example we use the
    // LoggingSpanExporter which prints on the console output the spans.

    // Configure the simple spans processor. This span processor exports span immediately after they
    // are ended.
    SimpleSpansProcessor simpleSpansProcessor = SimpleSpansProcessor.newBuilder(exporter).build();
    tracerProvider.addSpanProcessor(simpleSpansProcessor);

    // Configure the batch spans processor. This span processor exports span in batches.
    BatchSpansProcessor batchSpansProcessor =
        BatchSpansProcessor.newBuilder(exporter)
            .reportOnlySampled(true) // send to the exporter only spans that have been sampled
            .setMaxExportBatchSize(512) // set the maximum batch size to use
            .setMaxQueueSize(2048) // set the queue size. This must be >= the export batch size
            .setExporterTimeoutMillis(
                30_000) // set the max amount of time an export can run before getting interrupted
            .setScheduleDelayMillis(5000) // set time between two different exports
            .build();
    tracerProvider.addSpanProcessor(batchSpansProcessor);

    // Configure the multi spans processor. A MultiSpanProcessor accepts a list of Span Processors.
    SpanProcessor multiSpanProcessor =
        MultiSpanProcessor.create(Arrays.asList(simpleSpansProcessor, batchSpansProcessor));
    tracerProvider.addSpanProcessor(multiSpanProcessor);
  }

  private static void customSpanProcessor() throws Exception {
    // We can also implement our own SpanProcessor. It is only necessary to implement the respective
    // interface which requires three methods that are called during the lifespan of Spans.
    class MySpanProcessor implements SpanProcessor {

      @Override
      public void onStart(ReadableSpan span) {
        // This method is called when a span is created;
        System.out.printf("Span %s - Started\n", span.getName());
      }

      @Override
      public boolean isStartRequired() {
        // This method is called at initialization of the current SpanProcessor.
        // If this method returns true, onStart() will be called for every created span.
        return true;
      }

      @Override
      public void onEnd(ReadableSpan span) {
        // This method is called when a span is ended;
        System.out.printf("Span %s - Ended\n", span.getName());
      }

      @Override
      public boolean isEndRequired() {
        // This method is called at initialization of the current SpanProcessor.
        // If this method returns true, onEnd() will be called for every ended span.
        return true;
      }

      @Override
      public void shutdown() {
        // This method is called when the OpenTelemetry library is shutting down;
        System.out.printf("Goodbye by %s\n", this.getClass().getSimpleName());
      }

      @Override
      public void forceFlush() {
        // This method is useful for async Span Processors.
        // When this method is called, spans that are ended but not yet processed must be processed.
        System.out.println(
            "This is a sync implementation, so every span is processed within the onEnd() method");
      }

      @Override
      public String toString() {
        return "MySpanProcessor{}";
      }
    }

    // Attach to the configuration like any other SpanProcessor
    tracerProvider.addSpanProcessor(new MySpanProcessor());
  }
}
