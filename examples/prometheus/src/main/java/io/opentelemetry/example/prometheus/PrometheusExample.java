package io.opentelemetry.example.prometheus;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example of using the {@link PrometheusCollector} to convert OTel metrics to Prometheus format and
 * expose these to a Prometheus instance via a {@link HTTPServer} exporter.
 *
 * <p>A Gauge is used to periodically measure how many incoming messages are awaiting processing.
 * The Gauge callback gets executed every collection interval.
 */
public final class PrometheusExample {
  private long incomingMessageCount;

  public PrometheusExample(MeterProvider meterProvider) {
    Meter meter = meterProvider.get("PrometheusExample");
    meter
        .gaugeBuilder("incoming.messages")
        .setDescription("No of incoming messages awaiting processing")
        .setUnit("message")
        .buildWithCallback(result -> result.observe(incomingMessageCount, Attributes.empty()));
  }

  void simulate() {
    for (int i = 500; i > 0; i--) {
      try {
        System.out.println(
            i + " Iterations to go, current incomingMessageCount is:  " + incomingMessageCount);
        incomingMessageCount = ThreadLocalRandom.current().nextLong(100);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignored here
      }
    }
  }

  public static void main(String[] args) throws IOException {
    int prometheusPort = 0;
    try {
      prometheusPort = Integer.parseInt(args[0]);
    } catch (Exception e) {
      System.out.println("Port not set, or is invalid. Exiting");
      System.exit(1);
    }

    // it is important to initialize the OpenTelemetry SDK as early as possible in your process.
    MeterProvider meterProvider = ExampleConfiguration.initializeOpenTelemetry(prometheusPort);

    PrometheusExample prometheusExample = new PrometheusExample(meterProvider);

    prometheusExample.simulate();

    System.out.println("Exiting");

    // clean up the prometheus endpoint
    ExampleConfiguration.shutdownPrometheusEndpoint();
  }
}
