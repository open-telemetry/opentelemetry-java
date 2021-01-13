package io.opentelemetry.example.jaeger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.concurrent.TimeUnit;

public class JaegerExample {

  // OTel API
  private final SdkTracerManagement sdkTracerManagement;
  private final Tracer tracer;

  public JaegerExample(String ip, int port) {
    OpenTelemetrySdk sdk = initOpenTelemetry(ip, port);
    this.sdkTracerManagement = sdk.getTracerManagement();
    tracer = sdk.getTracer("io.opentelemetry.example.JaegerExample");
  }

  private OpenTelemetrySdk initOpenTelemetry(String ip, int port) {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel =
        ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
    // Export traces to Jaeger
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter =
        JaegerGrpcSpanExporter.builder()
            .setServiceName("otel-jaeger-example")
            .setChannel(jaegerChannel)
            .setTimeout(30, TimeUnit.SECONDS)
            .build();

    // Set to process the spans by the Jaeger Exporter
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                .build())
        .build();
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
    // note: this doesn't wait for everything to get cleaned up. We need an SDK update to enable
    // that.
    sdkTracerManagement.shutdown();
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
    example.initOpenTelemetry(ip, port);
    // generate a few sample spans
    for (int i = 0; i < 10; i++) {
      example.myWonderfulUseCase();
    }

    // Shutdown example
    example.shutdown();

    System.out.println("Bye");
  }
}
