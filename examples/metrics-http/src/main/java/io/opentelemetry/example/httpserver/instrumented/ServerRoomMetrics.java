package io.oopentelemetry.example.httpserver.instrumented;

import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;

/**
 * This class defines instruments to pull process (cpu load, e.g) metrics.
 *
 * <p>These are NOT attached to the global metric provider by default, so you can move them where
 * you need.
 */
public class ServerRoomMetrics {
  private final Meter metrics;
  private final DoubleValueObserver serverTemperature;
  private final Labels labels;

  private ServerRoomMetrics(Meter metrics) {
    this.metrics = metrics;
    // TODO: This should be labels for CPU metrics.
    this.labels = Labels.empty();
    this.serverTemperature =
        this.metrics
            .doubleValueObserverBuilder("room_temperature")
            .setDescription("the temperature of the server room")
            .setUnit("Fahrenheit")
            .setUpdater(result -> result.observe(getServerTemperature(), this.labels))
            .build();
  }

  /** For this demo, we construct a random number. IN practice this would be a real poll. */
  private double getServerTemperature() {
    return Math.random();
  }

  /** Creates process-metric readers against the given meter provider. */
  public static ServerRoomMetrics create(MeterProvider provider) {
    return new ServerRoomMetrics(
        provider.get("io.opentelemetry.example.metrics-http.serverroom", "0.1.0"));
  }
}
