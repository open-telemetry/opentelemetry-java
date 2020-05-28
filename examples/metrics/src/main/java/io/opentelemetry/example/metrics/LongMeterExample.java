package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.Meter;

public class LongMeterExample {

  public static void main(String[] args) {
    Meter sampleMeter = OpenTelemetry.getMeterProvider().get("sample", "0.1");
    LongCounter jvmUsageCounter = sampleMeter.longCounterBuilder("jvm_memory_usage")
        .setDescription("should meter jvm memory usage")
        .setUnit("byte")
        .build();
    long totalJvmMemory = Runtime.getRuntime().totalMemory();
    long freeJvmMemory = Runtime.getRuntime().freeMemory();
    // different key value parents will aggregate independently
    jvmUsageCounter.add(totalJvmMemory, "jvm memory", "total");
    jvmUsageCounter.add(freeJvmMemory, "jvm memory", "free");
  }

}
