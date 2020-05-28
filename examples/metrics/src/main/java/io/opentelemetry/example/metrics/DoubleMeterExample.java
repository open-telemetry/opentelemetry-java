package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.Meter;

public class DoubleMeterExample {

  public static void main(String[] args) {
    Meter sampleMeter = OpenTelemetry.getMeterProvider().get("sample", "0.1");
    DoubleCounter jvmUsageCounter = sampleMeter.doubleCounterBuilder("jvm_memory_usage")
        .setDescription("should meter jvm memory usage in percentage")
        .setUnit("%")
        .build();
    long totalJvmMemory = Runtime.getRuntime().totalMemory();
    long freeJvmMemory = Runtime.getRuntime().freeMemory();
    long usedJvmMemory = totalJvmMemory - freeJvmMemory;
    double usedJvmMemoryPercentage = ((usedJvmMemory * 1.0) / totalJvmMemory) * 100;
    jvmUsageCounter.add(usedJvmMemoryPercentage);
  }

}
