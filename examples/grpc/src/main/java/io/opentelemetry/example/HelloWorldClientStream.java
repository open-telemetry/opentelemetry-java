/*
 * Copyright 2015 The gRPC Authors
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.example;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
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

  // OTel API
  Tracer tracer =
      OpenTelemetry.getTracerProvider().get("io.opentelemetry.example.HelloWorldClient");;
  // Export traces as log
  LoggingSpanExporter exporter = new LoggingSpanExporter();
  // Share context via text headers
  HttpTextFormat textFormat = OpenTelemetry.getPropagators().getHttpTextFormat();
  // Inject context into the gRPC request metadata
  HttpTextFormat.Setter<Metadata> setter =
      new HttpTextFormat.Setter<Metadata>() {
        @Override
        public void set(Metadata carrier, String key, String value) {
          carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
        }
      };

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
    initTracer();
  }

  private void initTracer() {
    // Use the OpenTelemetry SDK
    TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
    // Set to process the spans by the log exporter
    tracerProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
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
    span.setAttribute("rpc.service", "Greeter");
    span.setAttribute("net.peer.ip", this.serverHostname);
    span.setAttribute("net.peer.port", this.serverPort);

    StreamObserver<HelloRequest> requestObserver = null;

    // Set the context with the current span
    try (Scope scope = tracer.withSpan(span)) {
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
      span.setStatus(Status.OK);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      // TODO create mapping for io.grpc.Status<->io.opentelemetry.trace.Status
      span.setStatus(Status.UNKNOWN.withDescription("gRPC status: " + e.getStatus()));
      return;
    } finally {
      span.end();
    }
  }

  private class HelloReplyStreamObserver implements StreamObserver<HelloReply> {

    public HelloReplyStreamObserver() {
      logger.info("Greeting: ");
    }

    @Override
    public void onNext(HelloReply value) {
      Span span = tracer.getCurrentSpan();
      span.addEvent("Data received: " + value.getMessage());
      logger.info(value.getMessage());
    }

    @Override
    public void onError(Throwable t) {
      Span span = tracer.getCurrentSpan();
      logger.log(Level.WARNING, "RPC failed: {0}", t.getMessage());
      // TODO create mapping for io.grpc.Status<->io.opentelemetry.trace.Status
      span.setStatus(Status.UNKNOWN.withDescription("gRPC status: " + t.getMessage()));
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
      return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
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
