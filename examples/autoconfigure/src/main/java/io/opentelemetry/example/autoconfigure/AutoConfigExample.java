package io.opentelemetry.example.autoconfigure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;

/**
 * An example of using {@link io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration}
 * and logging exporters: {@link io.opentelemetry.exporter.logging.LoggingSpanExporter} and {@link
 * io.opentelemetry.exporter.logging.LoggingMetricExporter}.
 */
public final class AutoConfigExample {
  private static final String INSTRUMENTATION_NAME = AutoConfigExample.class.getName();

  public static void main(String[] args) throws InterruptedException {
    // Export metrics every 800ms to avoid long Thread.sleep() at the end
    // You probably won't need to export metrics so often in a real life scenario
    System.setProperty("otel.imr.export.interval", "800");

    // Let the SDK configure itself using environment variables and system properties
    OpenTelemetry openTelemetry = OpenTelemetrySdkAutoConfiguration.initialize();

    AutoConfigExample example = new AutoConfigExample(openTelemetry);
    // Do some real work that'll emit telemetry
    example.doWork();

    // Flush out the metrics that have not yet been exported
    Thread.sleep(1000);
  }

  private final Tracer tracer;
  private final LongCounter counter;

  public AutoConfigExample(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    this.counter =
        GlobalMetricsProvider.getMeter(INSTRUMENTATION_NAME).longCounterBuilder("count").build();
  }

  public void doWork() throws InterruptedException {
    Span span =
        tracer
            .spanBuilder("important work")
            .setAttribute("foo", 42)
            .setAttribute("bar", "a string!")
            .startSpan();
    try {
      Thread.sleep(1000);
      counter.add(1);
    } finally {
      span.end();
    }
  }
}
