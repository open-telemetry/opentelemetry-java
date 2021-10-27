package io.opentelemetry.example.autoconfigure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;

/**
 * An example of using {@link io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration}
 * and logging exporter: {@link io.opentelemetry.exporter.logging.LoggingSpanExporter}.
 */
public final class AutoConfigExample {
  private static final String INSTRUMENTATION_NAME = AutoConfigExample.class.getName();

  public static void main(String[] args) throws InterruptedException {
    // Let the SDK configure itself using environment variables and system properties
    OpenTelemetry openTelemetry = OpenTelemetrySdkAutoConfiguration.initialize();

    AutoConfigExample example = new AutoConfigExample(openTelemetry);
    // Do some real work that'll emit telemetry
    example.doWork();
  }

  private final Tracer tracer;

  public AutoConfigExample(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
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
    } finally {
      span.end();
    }
  }
}
