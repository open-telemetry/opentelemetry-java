package io.opentelemetry.example.zipkin;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class ZipkinExample {
  // Zipkin API Endpoints for uploading spans
  private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";

  // Name of the service
  private static final String SERVICE_NAME = "myExampleService";

  // SDK Tracer Management interface
  private static SdkTracerManagement sdkTracerManagement;

  // The Tracer we'll use for the example
  private final Tracer tracer;

  public ZipkinExample(TracerProvider tracerProvider) {
    tracer = tracerProvider.get("io.opentelemetry.example.ZipkinExample");
  }

  // This method instruments doWork() method
  public void myWonderfulUseCase() {
    // Generate span
    Span span = tracer.spanBuilder("Start my wonderful use case").startSpan();
    try(Scope scope = span.makeCurrent()) {
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

    //it is important to initialize the OpenTelemetry SDK as early as possible in your process.
    OpenTelemetry openTelemetry = initializeOpenTelemetry(ip, port);

    TracerProvider tracerProvider = openTelemetry.getTracerProvider();

    // start example
    ZipkinExample example = new ZipkinExample(tracerProvider);
    example.myWonderfulUseCase();

    // shutdown example
    shutdownTheSdk();

    System.out.println("Bye");
  }

  // This method adds SimpleSpanProcessor initialized with ZipkinSpanExporter to the
  // TracerSdkProvider
  private static OpenTelemetry initializeOpenTelemetry(String ip, int port) {
    OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().build();

    String httpUrl = String.format("http://%s:%s", ip, port);
    ZipkinSpanExporter zipkinExporter =
        ZipkinSpanExporter.builder()
            .setEndpoint(httpUrl + ENDPOINT_V2_SPANS)
            .setServiceName(SERVICE_NAME)
            .build();
    // save the management interface so we can shut it down at the end of the example.
    sdkTracerManagement = openTelemetrySdk.getTracerManagement();
    // Set to process the spans by the Zipkin Exporter
    sdkTracerManagement.addSpanProcessor(SimpleSpanProcessor.builder(zipkinExporter).build());

    //return the configured instance so it can be used for instrumentation.
    return openTelemetrySdk;
  }

  // graceful shutdown
  public static void shutdownTheSdk() {
    sdkTracerManagement.shutdown();
  }
}
