package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.Meter;


/**
 * Example of using {@link DoubleCounter} to measure execution time of method.
 */
public class DoubleMeterExample {

  public static void main(String[] args) {
    Meter sampleMeter = OpenTelemetry.getMeterProvider()
        .get("io.opentelemetry.example.metrics", "0.5");
    DoubleCounter timeCounter = sampleMeter.doubleCounterBuilder("some_method_time_usage")
        .setDescription("should measure some method execution time")
        .setUnit("second")
        .build();
    Long timeStart = System.currentTimeMillis();
    superLongMethod();
    Long timeEnd = System.currentTimeMillis();
    Double seconds = (timeEnd.doubleValue() - timeStart.doubleValue()) / 1000;
    timeCounter.add(seconds, "someWork", "execution time");
  }

  private static void superLongMethod() {
    try {
      Thread.sleep(3_125);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
