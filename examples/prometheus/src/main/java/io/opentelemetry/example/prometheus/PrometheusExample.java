package io.opentelemetry.example.prometheus;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.AsynchronousInstrument;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Example of using the {@link PrometheusCollector} to convert OTel metrics to Prometheus format and
 * expose these to a Prometheus instance via a {@link HTTPServer} exporter.
 *
 * <p>A {@link LongValueObserver} is used to periodically measure how many incoming messages are
 * awaiting processing. The {@link LongValueObserver.Callback} gets executed every collection
 * interval.
 */
public class PrometheusExample {

  private final MeterSdkProvider meterSdkProvider = OpenTelemetrySdk.getGlobalMeterProvider();
  private final Meter meter = meterSdkProvider.get("PrometheusExample", "0.7");
  private final HTTPServer server;
  private long incomingMessageCount;

  public PrometheusExample(int port) throws IOException {

    LongValueObserver observer =
        meter
            .longValueObserverBuilder("incoming.messages")
            .setDescription("No of incoming messages awaiting processing")
            .setUnit("message")
            .build();

    observer.setCallback(
        new LongValueObserver.Callback<LongValueObserver.LongResult>() {
          @Override
          public void update(AsynchronousInstrument.LongResult result) {
            result.observe(incomingMessageCount, Labels.empty());
          }
        });

    PrometheusCollector.builder()
        .setMetricProducer(meterSdkProvider.getMetricProducer())
        .buildAndRegister();

    server = new HTTPServer(port);
  }

  void shutdown() {
    server.stop();
  }

  void simulate() {
    for (int i = 300; i > 0; i--) {
      try {
        System.out.println(
            i + " Iterations to go, current incomingMessageCount is:  " + incomingMessageCount);
        incomingMessageCount = ThreadLocalRandom.current().nextLong(100);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
  }

  public static void main(String[] args) throws IOException {

    int port = 0;
    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception e) {
      System.out.println("Port not set, or is invalid. Exiting");
      System.exit(1);
    }

    PrometheusExample prometheusExample = new PrometheusExample(port);

    prometheusExample.simulate();
    prometheusExample.shutdown();

    System.out.println("Exiting");
  }
}
