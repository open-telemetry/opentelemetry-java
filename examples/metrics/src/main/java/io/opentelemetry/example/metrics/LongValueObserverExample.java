package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.AsynchronousInstrument.LongResult;
import io.opentelemetry.metrics.LongValueObserver;
import io.opentelemetry.metrics.Meter;

/**
 * Example of using {@link LongValueObserver} to measure execution time of method. Setting {@link
 * LongValueObserver.Callback} a callback that gets executed every collection interval. Useful for
 * expensive measurements that would be wastefully to calculate each request.
 */
public class LongValueObserverExample {

  public static void main(String[] args) {
    Meter sampleMeter =
        OpenTelemetry.getMeterProvider().get("io.opentelemetry.example.metrics", "0.5");
    LongValueObserver observer =
        sampleMeter
            .longValueObserverBuilder("jvm.memory.total")
            .setDescription("Reports JVM memory usage.")
            .setUnit("byte")
            .build();

    observer.setCallback(
        new LongValueObserver.Callback<LongValueObserver.LongResult>() {
          @Override
          public void update(LongResult result) {
            result.observe(Runtime.getRuntime().totalMemory(), Labels.empty());
          }
        });
    // someWork();
  }
}
