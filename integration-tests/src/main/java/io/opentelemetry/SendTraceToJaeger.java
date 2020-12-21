/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class SendTraceToJaeger {
  // Jaeger Endpoint URL and PORT
  private final String ip; // = "jaeger";
  private final int port; // = 14250;

  public SendTraceToJaeger(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  private OpenTelemetry initOpenTelemetry() {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setServiceName("integration test")
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
            .build();

    // Set to process the spans by the Jaeger Exporter
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter).build())
                .build())
        .build();
  }

  private static void myWonderfulUseCase(OpenTelemetry openTelemetry) {
    // Generate a span
    Span span =
        openTelemetry
            .getTracer("io.opentelemetry.SendTraceToJaeger")
            .spanBuilder("Start my wonderful use case")
            .startSpan();
    span.addEvent("Event 0");
    // execute my use case - here we simulate a wait
    doWait();
    span.addEvent("Event 1");
    span.end();
  }

  private static void doWait() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // catch
    }
  }

  /**
   * Main method.
   *
   * @param args args
   */
  public static void main(String[] args) {
    // Parsing the input
    if (args.length < 2) {
      System.out.println("Missing [hostname] [port]");
      System.exit(1);
    }
    String ip = args[0];
    int port = Integer.parseInt(args[1]);

    // Start the example
    SendTraceToJaeger example = new SendTraceToJaeger(ip, port);
    OpenTelemetry openTelemetry = example.initOpenTelemetry();
    SendTraceToJaeger.myWonderfulUseCase(openTelemetry);
    // wait some seconds
    doWait();
    System.out.println("Bye");
  }
}
