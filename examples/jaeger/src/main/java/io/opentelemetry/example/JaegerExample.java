package io.opentelemetry.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

public class JaegerExample {
  // Jaeger Endpoint URL and PORT
  String ip; // = "jaeger";
  int port; // = 14250;
  // OTel API
  Tracer tracer;
  // Create a channel towards Jaeger end point
  ManagedChannel jaegerChannel;
  // Export traces to Jaeger
  JaegerGrpcSpanExporter jaegerExporter;

  private void initTracer() {
    // Create a channel towards Jaeger end point
    jaegerChannel = ManagedChannelBuilder.forAddress(ip, port).build();
    // Export traces to Jaeger
    jaegerExporter =
        JaegerGrpcSpanExporter.newBuilder()
            .setServiceName("example")
            .setChannel(jaegerChannel)
            .setDeadline(10)
            .build();

    // Get the tracer
    TracerSdkFactory tracerFactory = OpenTelemetrySdk.getTracerFactory();
    // Set to process the spans by the Jaeger Exporter
    tracerFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(jaegerExporter).build());
    // Give a name to the tracer
    this.tracer = tracerFactory.get("io.opentelemetry.example.JaegerExample");
  }

  private void myWonderfulUseCase() {
    initTracer();
    Span span = tracer.spanBuilder("Start my wonderful use case").startSpan();
    span.addEvent("Event 0");
    // execute my use case - here we simulate a wait
    doWork();
    span.addEvent("Event 1");
    span.end();
    jaegerExporter.shutdown();
  }

  private void doWork() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
  }

  public JaegerExample(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Missing [hostname] [port]");
      System.exit(1);
    }
    String ip = args[0];
    int port = Integer.parseInt(args[1]);
    JaegerExample example = new JaegerExample(ip, port);
    example.myWonderfulUseCase();
    System.out.println("Bye");
  }
}
