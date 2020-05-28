package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.LongValueObserver;
import io.opentelemetry.metrics.LongValueObserver.ResultLongValueObserver;
import io.opentelemetry.metrics.Meter;

public class ObserverMeterExample {

  public static void main(String[] args) {
    Meter sampleMeter = OpenTelemetry.getMeterProvider().get("sample", "0.1");
    LongValueObserver observer = sampleMeter.longValueObserverBuilder("jvm_memory_usage")
        .setDescription("should meter jvm memory usage")
        .setUnit("byte")
        .build();

    observer.setCallback(
        new LongValueObserver.Callback<LongValueObserver.ResultLongValueObserver>() {
          @Override
          public void update(ResultLongValueObserver result) {
            result.observe(Runtime.getRuntime().totalMemory(), "jvm memory", "free");
          }
        });
  }
}
