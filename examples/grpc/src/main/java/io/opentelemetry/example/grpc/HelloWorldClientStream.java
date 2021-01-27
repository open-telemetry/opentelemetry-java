/*
 * Copyright 2015 The gRPC Authors
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldClientStream {
  private static final Logger logger = Logger.getLogger(HelloWorldClientStream.class.getName());
  private final ManagedChannel channel;
  private final String serverHostname;
  private final Integer serverPort;
  private final GreeterGrpc.GreeterStub asyncStub;

  // Export spans as log entries
  private static final LoggingSpanExporter exporter = new LoggingSpanExporter();
  // OTel API
  private static final OpenTelemetry openTelemetry = initOpenTelemetry(exporter);

  private final Tracer tracer =
      openTelemetry.getTracer("io.opentelemetry.example.HelloWorldClient");
  // Share context via text headers
  private final TextMapPropagator textFormat =
      openTelemetry.getPropagators().getTextMapPropagator();
  // Inject context into the gRPC request metadata
  private final TextMapPropagator.Setter<Metadata> setter =
      (carrier, key, value) ->
          carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);

  /** Construct client connecting to HelloWorld server at {@code host:port}. */
  public HelloWorldClientStream(String host, int port) {
    this.serverHostname = host;
    this.serverPort = port;
    this.channel =
        ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            // Intercept the request to tag the span context
            .intercept(new OpenTelemetryClientInterceptor())
            .build();
    asyncStub = GreeterGrpc.newStub(channel);
    // Initialize the OTel tracer
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Say hello to server. */
  public void greet(List<String> names) {
    logger.info("Will try to greet " + Arrays.toString(names.toArray()) + " ...");

    // Start a span
    Span span =
        tracer.spanBuilder("helloworld.Greeter/SayHello").setSpanKind(Span.Kind.CLIENT).startSpan();
    span.setAttribute("component", "grpc");
    span.setAttribute(SemanticAttributes.RPC_SERVICE, "Greeter");
    span.setAttribute(SemanticAttributes.NET_HOST_IP, this.serverHostname);
    span.setAttribute(SemanticAttributes.NET_PEER_PORT, this.serverPort);

    StreamObserver<HelloRequest> requestObserver;

    // Set the context with the current span
    try (Scope scope = span.makeCurrent()) {
      HelloReplyStreamObserver replyObserver = new HelloReplyStreamObserver();
      requestObserver = asyncStub.sayHelloStream(replyObserver);
      for (String name : names) {
        try {
          requestObserver.onNext(HelloRequest.newBuilder().setName(name).build());
          // Sleep for a bit before sending the next one.
          Thread.sleep(500);
        } catch (InterruptedException e) {
          logger.log(Level.WARNING, "RPC failed: {0}", e.getMessage());
          requestObserver.onError(e);
        }
      }
      requestObserver.onCompleted();
      span.addEvent("Done sending");
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      span.setStatus(StatusCode.ERROR, "gRPC status: " + e.getStatus());
    } finally {
      span.end();
    }
  }

  private static class HelloReplyStreamObserver implements StreamObserver<HelloReply> {

    public HelloReplyStreamObserver() {
      logger.info("Greeting: ");
    }

    @Override
    public void onNext(HelloReply value) {
      Span span = Span.current();
      span.addEvent("Data received: " + value.getMessage());
      logger.info(value.getMessage());
    }

    @Override
    public void onError(Throwable t) {
      Span span = Span.current();
      logger.log(Level.WARNING, "RPC failed: {0}", t.getMessage());
      span.setStatus(StatusCode.ERROR, "gRPC status: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
      // Since onCompleted is async and the span.end() is called in the main thread,
      // it is recommended to set the span Status in the main thread.
    }
  }

  public class OpenTelemetryClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
      return new ForwardingClientCall.SimpleForwardingClientCall<>(
          channel.newCall(methodDescriptor, callOptions)) {
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          // Inject the request with the current context
          textFormat.inject(Context.current(), headers, setter);
          // Perform the gRPC request
          super.start(responseListener, headers);
        }
      };
    }
  }

  private static OpenTelemetry initOpenTelemetry(LoggingSpanExporter exporter) {
    // Set to process the the spans by the LogExporter
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();

    // install the W3C Trace Context propagator
    return OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .build();
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting.
   */
  public static void main(String[] args) throws Exception {
    // Access a service running on the local machine on port 50051
    HelloWorldClientStream client = new HelloWorldClientStream("localhost", 50051);
    try {
      List<String> users = Arrays.asList("world", "this", "is", "a", "list", "of", "names");
      // Use the arg as the name to greet if provided
      if (args.length > 0) {
        users = Arrays.asList(args);
      }
      client.greet(users);
    } finally {
      client.shutdown();
    }
  }
}
