/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.grpc;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

class ExampleConfiguration {

  static OpenTelemetry initOpenTelemetry() {

    // Set to process the spans with the LoggingSpanExporter
    LoggingSpanExporter exporter = new LoggingSpanExporter();
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

    OpenTelemetrySdk openTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            // install the W3C Trace Context propagator
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();

    // it's always a good idea to shutdown the SDK when your process exits.
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.err.println(
                      "*** forcing the Span Exporter to shutdown and process the remaining spans");
                  sdkTracerProvider.shutdown();
                  System.err.println("*** Trace Exporter shut down");
                }));

    return openTelemetrySdk;
  }
}
