/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.zipkin;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
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

  // SDK Tracer Management interface
  private static SdkTracerManagement sdkTracerManagement;

  // This method adds SimpleSpanProcessor initialized with ZipkinSpanExporter to the
  // TracerSdkProvider
  static OpenTelemetry initializeOpenTelemetry(String ip, int port) {
    OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().build();

    String httpUrl = String.format("http://%s:%s", ip, port);
    ZipkinSpanExporter zipkinExporter =
        ZipkinSpanExporter.builder()
            .setEndpoint(httpUrl + ENDPOINT_V2_SPANS)
            .setServiceName(SERVICE_NAME)
            .build();
    // save the management interface so we can shut it down at the end of the example.
    sdkTracerManagement = openTelemetrySdk.getTracerManagement();
    // Set to process the spans by the Zipkin Exporter
    sdkTracerManagement.addSpanProcessor(SimpleSpanProcessor.builder(zipkinExporter).build());

    // return the configured instance so it can be used for instrumentation.
    return openTelemetrySdk;
  }

  // graceful shutdown
  static void shutdownTheSdk() {
    sdkTracerManagement.shutdown();
  }
}
