package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.metrics.Meter;
import java.util.Random;

/**
 * Long meter example with bound counter
 */
public class LongMeterExample {

  private static final Meter sampleMeter = OpenTelemetry.getMeterProvider()
      .get("io.opentelemetry.example.metrics", "0.5");
  private static final LongCounter methodCallCounter = sampleMeter
      .longCounterBuilder("method_call_counter")
      .setDescription("should count methods call")
      .setUnit("unit")
      .build();
  //we can use BoundCounters to not specify labels each time
  private static final BoundLongCounter gateBCounter = methodCallCounter.bind("gate", "b");

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      abGate();
    }
  }

  //compare bound and usual counter usage
  private static void abGate() {
    if (new Random().nextBoolean()) {
      methodCallCounter.add(1, "gate", "a");
    } else {
      gateBCounter.add(1);
    }
  }
}
