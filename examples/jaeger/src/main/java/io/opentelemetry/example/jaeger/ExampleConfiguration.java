/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.jaeger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.concurrent.TimeUnit;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
class ExampleConfiguration {

  /**
   * Initialize an OpenTelemetry SDK with a Jaeger exporter and a SimpleSpanProcessor.
   * @param jaegerHost The host of your Jaeger instance.
   * @param jaegerPort the port of your Jaeger instance.
   * @return A ready-to-use {@link OpenTelemetry} instance.
   */
  static OpenTelemetry initOpenTelemetry(String jaegerHost, int jaegerPort) {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(jaegerHost, jaegerPort).usePlaintext().build();
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setServiceName("otel-jaeger-example")
            .setChannel(jaegerChannel)
            .setTimeout(30, TimeUnit.SECONDS)
            .build();

    // Set to process the spans by the Jaeger Exporter
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .build())
        .build();

    //it's always a good idea to shut down the SDK cleanly at JVM exit.
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> openTelemetry.getTracerManagement().shutdown()));

    return openTelemetry;
  }
}
