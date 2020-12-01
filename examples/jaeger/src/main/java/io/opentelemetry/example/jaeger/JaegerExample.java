package io.opentelemetry.example.jaeger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class JaegerExample {

  // Jaeger Endpoint URL and PORT
  private final String ip; // = "jaeger";
  private final int port; // = 14250;

  // OTel API
  private final Tracer tracer =
      OpenTelemetry.getGlobalTracer("io.opentelemetry.example.JaegerExample");

  public JaegerExample(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  private void setupJaegerExporter() {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
    // Export traces to Jaeger
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setServiceName("otel-jaeger-example")
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
            .build();

    // Set to process the spans by the Jaeger Exporter
    OpenTelemetrySdk.getGlobalTracerManagement()
        .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter).build());
  }

  private void myWonderfulUseCase() {
    // Generate a span
    Span span = this.tracer.spanBuilder("Start my wonderful use case").startSpan();
    span.addEvent("Event 0");
    // execute my use case - here we simulate a wait
    doWork();
    span.addEvent("Event 1");
    span.end();
  }

  private void doWork() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // do the right thing here
    }
  }

  // graceful shutdown
  public void shutdown() {
    OpenTelemetrySdk.getGlobalTracerManagement().shutdown();
  }

  public static void main(String[] args) {
    // Parsing the input
    if (args.length < 2) {
      System.out.println("Missing [hostname] [port]");
      System.exit(1);
    }
    String ip = args[0];
    int port = Integer.parseInt(args[1]);

    // Start the example
    JaegerExample example = new JaegerExample(ip, port);
    example.setupJaegerExporter();
    // generate a few sample spans
    for (int i = 0; i < 10; i++) {
      example.myWonderfulUseCase();
    }

    // Shutdown example
    example.shutdown();

    System.out.println("Bye");
  }
}
