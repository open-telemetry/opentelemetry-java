package io.opentelemetry.example.zipkin;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;

public class ZipkinExample {
  // The Tracer we'll use for the example
  private final Tracer tracer;

  public ZipkinExample(TracerProvider tracerProvider) {
    tracer = tracerProvider.get("io.opentelemetry.example.ZipkinExample");
  }

  // This method instruments doWork() method
  public void myWonderfulUseCase() {
    // Generate span
    Span span = tracer.spanBuilder("Start my wonderful use case").startSpan();
    try (Scope scope = span.makeCurrent()) {
      // Add some Event to the span
      span.addEvent("Event 0");
      // execute my use case - here we simulate a wait
      doWork();
      // Add some Event to the span
      span.addEvent("Event 1");
    } finally {
      span.end();
    }
  }

  public void doWork() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore in an example
    }
  }

  public static void main(String[] args) {
    // Parsing the input
    if (args.length < 2) {
      System.out.println("Missing [hostname] [port]");
      System.exit(1);
    }

    String ip = args[0];
    int port = Integer.parseInt(args[1]);

    // it is important to initialize the OpenTelemetry SDK as early as possible in your process.
    OpenTelemetry openTelemetry = ExampleConfiguration.initializeOpenTelemetry(ip, port);

    TracerProvider tracerProvider = openTelemetry.getTracerProvider();

    // start example
    ZipkinExample example = new ZipkinExample(tracerProvider);
    example.myWonderfulUseCase();

    // shutdown example
    ExampleConfiguration.shutdownTheSdk();

    System.out.println("Bye");
  }
}
