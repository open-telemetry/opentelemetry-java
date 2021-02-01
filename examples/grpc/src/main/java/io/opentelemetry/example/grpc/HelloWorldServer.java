/*
 * Copyright 2015 The gRPC Authors
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.grpc;

import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/** Server that manages startup/shutdown of a {@code Greeter} server. */
public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

  private static final int PORT = 50051;

  // it is important to initialize the OpenTelemetry SDK as early as possible in your application's
  // lifecycle.
  private static final OpenTelemetry openTelemetry = ExampleConfiguration.initOpenTelemetry();

  // Extract the Distributed Context from the gRPC metadata
  private static final TextMapPropagator.Getter<Metadata> getter =
      new TextMapPropagator.Getter<>() {
        @Override
        public Iterable<String> keys(Metadata carrier) {
          return carrier.keys();
        }

        @Override
        public String get(Metadata carrier, String key) {
          Metadata.Key<String> k = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
          if (carrier.containsKey(k)) {
            return carrier.get(k);
          }
          return "";
        }
      };

  private Server server;

  private final Tracer tracer =
      openTelemetry.getTracer("io.opentelemetry.example.HelloWorldServer");
  private final TextMapPropagator textFormat =
      openTelemetry.getPropagators().getTextMapPropagator();

  private void start() throws IOException {
    /* The port on which the server should run */

    server =
        ServerBuilder.forPort(PORT)
            .addService(new GreeterImpl())
            // Intercept gRPC calls
            .intercept(new OpenTelemetryServerInterceptor())
            .build()
            .start();
    logger.info("Server started, listening on " + PORT);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("*** shutting down gRPC server since JVM is shutting down");
                  HelloWorldServer.this.stop();
                  System.err.println("*** server shut down");
                }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    // We serve a normal gRPC call
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      // Serve the request
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }

    // We serve a stream gRPC call
    @Override
    public StreamObserver<HelloRequest> sayHelloStream(
        final StreamObserver<HelloReply> responseObserver) {
      return new StreamObserver<>() {
        @Override
        public void onNext(HelloRequest value) {
          responseObserver.onNext(
              HelloReply.newBuilder().setMessage("Hello " + value.getName()).build());
        }

        @Override
        public void onError(Throwable t) {
          logger.info("[Error] " + t.getMessage());
          responseObserver.onError(t);
        }

        @Override
        public void onCompleted() {
          responseObserver.onCompleted();
        }
      };
    }
  }

  private class OpenTelemetryServerInterceptor implements io.grpc.ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
      // Extract the Span Context from the metadata of the gRPC request
      Context extractedContext = textFormat.extract(Context.current(), headers, getter);
      InetSocketAddress clientInfo =
          (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
      // Build a span based on the received context
      Span span =
          tracer
              .spanBuilder("helloworld.Greeter/SayHello")
              .setParent(extractedContext)
              .setSpanKind(Span.Kind.SERVER)
              .startSpan();
      try (Scope innerScope = span.makeCurrent()) {
        span.setAttribute("component", "grpc");
        span.setAttribute("rpc.service", "Greeter");
        span.setAttribute("net.peer.ip", clientInfo.getHostString());
        span.setAttribute("net.peer.port", clientInfo.getPort());
        // Process the gRPC call normally
        return Contexts.interceptCall(io.grpc.Context.current(), call, headers, next);
      } finally {
        span.end();
      }
    }
  }

  /** Main launches the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    final HelloWorldServer server = new HelloWorldServer();
    server.start();
    server.blockUntilShutdown();
  }
}
