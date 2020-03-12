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

import io.opentelemetry.exporters.inmemory.InMemorySpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.example.unsafe.DemoUtils;
import io.opentelemetry.sdk.trace.MultiSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.BatchSpansProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;

import java.util.Arrays;

/** This example shows how to instantiate different Span Processors. */
class ConfigureSpanProcessorExample {

  static InMemorySpanExporter exporter = InMemorySpanExporter.create();

  public static void main(String[] args) throws Exception {
    // Get the Tracer Provider
    TracerSdkFactory tracerProvider = OpenTelemetrySdk.getTracerFactory();

    // Configure the simple spans processor.
    tracerProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());

    // Now we can acquire a tracer
    TracerSdk tracer = tracerProvider.get("ConfigureSpanProcessorExample");
    // Print to the console the list of span processors enabled.
    DemoUtils.printProcessorList(tracer);

    // Processors that are plugged after the tracer creation are still propagated to the tracer.
    tracerProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
    // Print to the console the list of span processors enabled.
    DemoUtils.printProcessorList(tracer);

    // We can also implement our own SpanProcessor. It is necessary to only implement the relative
    // interface
    // which requires three methods that are called during the lifespan of Spans.
    class MySpanProcessor implements SpanProcessor {

      @Override
      public void onStart(ReadableSpan span) {
        // This method is called when a span is created;
        System.out.printf("Span %s - Started\n", span.getName());
      }

      @Override
      public void onEnd(ReadableSpan span) {
        // This method is called when a span is terminated;
        System.out.printf("Span %s - Terminated\n", span.getName());
      }

      @Override
      public void shutdown() {
        // This method is called when the OpenTelemetry library is shutting down;
        System.out.printf("Goodbye by %s\n", this.getClass().getSimpleName());
      }

      @Override
      public String toString() {
        return "MySpanProcessor{}";
      }
    }

    // We can also configure multiple span processors at the same time using the MultiSpanProcessor
    // class.
    // MultiSpanProcessor can be nested.
    tracerProvider.addSpanProcessor(
        MultiSpanProcessor.create(
            Arrays.asList(
                new MySpanProcessor(),
                MultiSpanProcessor.create(
                    Arrays.asList(
                        SimpleSpansProcessor.newBuilder(exporter).build(),
                        BatchSpansProcessor.newBuilder(exporter).build())))));
    // Print to the console the list of span processors enabled.
    DemoUtils.printProcessorList(tracer);

    // We generate some Spans so we can test MySpanProcessor implementation
    tracer.spanBuilder("Span #1").startSpan().end();
    tracer.spanBuilder("Span #2").startSpan().end();
    tracer.spanBuilder("Span #3").startSpan().end();

    // We shutdown the OpenTelemetry library
    // This automatically shuts down
    OpenTelemetrySdk.getTracerFactory().shutdown();
  }
}
