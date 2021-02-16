package io.opentelemetry.example.httpserver.application;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.oopentelemetry.example.httpserver.instrumented.InstrumentedHttpServer;
import io.oopentelemetry.example.httpserver.instrumented.ProcessMetrics;
import io.oopentelemetry.example.httpserver.instrumented.ServerRoomMetrics;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class Application {
  public static void main(String[] args) throws IOException {
    // First we grab access to all global metrics.
    final SdkMeterProvider globalMetrics =
        SdkMeterProvider.builder()
            // Make sure we hijack "global" metrics to ensure our HTTP library metrics are reported.
            .buildAndRegisterGlobal();

    // Next we instantiate the ProcessMetrics against this global metrics.
    final ProcessMetrics processMetrics = ProcessMetrics.create(globalMetrics);
    // Next we attach an interval for pull-metrics that wires our global metrics to an exporter.
    IntervalMetricReader globalReader =
        IntervalMetricReader.builder()
            // Collect metrics every 5 seconds.
            .setExportIntervalMillis(5000)
            // Capture ALL global metrics from any library.
            .setMetricProducers(Collections.singleton(globalMetrics))
            // For now just log the metrics.
            .setMetricExporter(new LoggingMetricExporter())
            .build();

    // Next we want to create the server room metrics at a LONGER poll interval.  In
    // today's API this requires a separate metric provider + interval reader.
    final SdkMeterProvider serverRoomProvider = SdkMeterProvider.builder().build();
    final ServerRoomMetrics serverRoomMetrics = ServerRoomMetrics.create(serverRoomProvider);
    IntervalMetricReader roomReader =
        IntervalMetricReader.builder()
            // Collect metrics every minute.
            .setExportIntervalMillis(60000)
            // Here we list ONLY the server room provider.
            .setMetricProducers(Collections.singleton(serverRoomProvider))
            // For now just log the metrics.
            .setMetricExporter(new LoggingMetricExporter())
            .build();

    // Register the server.
    final InstrumentedHttpServer server = new InstrumentedHttpServer(8080);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    // Register HTTP handlers.
    server.createContext("/", new HelloHandler());
    server.start();
  }

  private static class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      byte[] response = "Hello, Server world!".getBytes(UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(response);
      }
    }
  }
}
