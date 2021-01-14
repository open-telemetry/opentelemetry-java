/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.zipkin;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
public class ExampleConfiguration {
  // Zipkin API Endpoints for uploading spans
  private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";

  // Name of the service
  private static final String SERVICE_NAME = "myExampleService";

  /** Adds a SimpleSpanProcessor initialized with ZipkinSpanExporter to the TracerSdkProvider */
  static OpenTelemetry initializeOpenTelemetry(String ip, int port) {
    String httpUrl = String.format("http://%s:%s", ip, port);
    ZipkinSpanExporter zipkinExporter =
        ZipkinSpanExporter.builder()
            .setEndpoint(httpUrl + ENDPOINT_V2_SPANS)
            .setServiceName(SERVICE_NAME)
            .build();

    // Set to process the spans by the Zipkin Exporter
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(zipkinExporter))
                    .build())
            .buildAndRegisterGlobal();

    // add a shutdown hook to shut down the SDK
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> openTelemetry.getTracerManagement().shutdown()));

    // return the configured instance so it can be used for instrumentation.
    return openTelemetry;
  }
}
