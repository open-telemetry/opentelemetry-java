package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;


/**
 * Example of using {@link DoubleCounter} to measure execution time of method.
 */
public class DoubleCounterExample {

  private static final Tracer tracer =
      OpenTelemetry.getTracer("io.opentelemetry.example.metrics");

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("superLongMethod")
        .setSpanKind(Kind.INTERNAL)
        .startSpan();
    try (Scope scope = tracer.withSpan(span)) {
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
      //we can add values to counter without addition label key-values pairs
      timeCounter.add(seconds);
    } catch (Exception e) {
      Status status = Status.UNKNOWN.withDescription("Error while finding file");
      span.setStatus(status);
    } finally {
      span.end();
    }
  }

  private static void superLongMethod() {
    try {
      Thread.sleep(3_125);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
