package io.oopentelemetry.example.httpserver.application;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.oopentelemetry.example.httpserver.instrumented.InstrumentedHttpServer;
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
    // Next we attach an interval for pull-metrics that wires our global metrics to an exporter.
    IntervalMetricReader reader =
        IntervalMetricReader.builder()
            // Collect metrics every 2 seconds.
            .setExportIntervalMillis(20000)
            // Here we list which metrics we'll be exporting.
            .setMetricProducers(Collections.singleton(globalMetrics))
            // For now just log the metrics.
            .setMetricExporter(new LoggingMetricExporter())
            .build();

    // Register the server.
    final InstrumentedHttpServer server = new InstrumentedHttpServer(8080);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    // Register HTTP handlers.
    server.creeateContext("/", new HelloHandler());
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
