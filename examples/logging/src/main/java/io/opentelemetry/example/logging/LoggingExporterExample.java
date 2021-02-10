package io.opentelemetry.example.logging;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

/**
 * An example of using {@link io.opentelemetry.exporter.logging.LoggingSpanExporter} and {@link
 * io.opentelemetry.exporter.logging.LoggingMetricExporter}.
 */
public class LoggingExporterExample {
  private static final String INSTRUMENTATION_NAME = LoggingExporterExample.class.getName();

  private final Tracer tracer;
  private final LongCounter counter;

  public LoggingExporterExample(OpenTelemetry openTelemetry) {
    tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    counter =
        GlobalMetricsProvider.getMeter(INSTRUMENTATION_NAME)
            .longCounterBuilder("work_done")
            .build();
  }

  public void myWonderfulUseCase() {
    Span span = this.tracer.spanBuilder("start my wonderful use case").startSpan();
    span.addEvent("Event 0");
    doWork();
    span.addEvent("Event 1");
    span.end();
  }

  private void doWork() {
    Span span = this.tracer.spanBuilder("doWork").startSpan();
    try {
      Thread.sleep(1000);
      counter.add(1);
    } catch (InterruptedException e) {
      // do the right thing here
    } finally {
      span.end();
    }
  }

  public static void main(String[] args) {
    // it is important to initialize your SDK as early as possible in your application's lifecycle
    OpenTelemetry oTel = ExampleConfiguration.initOpenTelemetry();

    // Start the example
    LoggingExporterExample example = new LoggingExporterExample(oTel);
    // Generate a few sample spans
    for (int i = 0; i < 5; i++) {
      example.myWonderfulUseCase();
    }

    try {
      // Flush out the metrics that have not yet been exported
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
    }

    System.out.println("Bye");
  }
}
