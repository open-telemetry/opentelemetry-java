package io.opentelemetry.example.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.Meter;

/**
 * Example of using {@link LongValueObserver} to measure execution time of method. Setting {@link
 * LongValueObserver.Callback} a callback that gets executed every collection interval. Useful for
 * expensive measurements that would be wastefully to calculate each request.
 */
public class LongValueObserverExample {

  public static void main(String[] args) {
    Meter sampleMeter = OpenTelemetry.getGlobalMeter("io.opentelemetry.example.metrics", "0.5");
    LongValueObserver observer =
        sampleMeter
            .longValueObserverBuilder("jvm.memory.total")
            .setDescription("Reports JVM memory usage.")
            .setUnit("byte")
            .setCallback(
                result -> result.observe(Runtime.getRuntime().totalMemory(), Labels.empty()))
            .build();
  }
}
