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

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.inmemory.InMemorySpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());
    private Server server;

    // Set a Context Key to access the Distributed Context
    public static final Context.Key<SpanContext> TRACE_ID_CTX_KEY = Context.key("traceId");

    // OTel API
    Tracer OTel;
    // Export traces in memory
    InMemorySpanExporter inMemexporter = InMemorySpanExporter.create();
    // Share context via text
    HttpTextFormat<SpanContext> textFormat;

    // Extract the Distributed Context from the gRPC metadata
    HttpTextFormat.Getter<Metadata> getter = new HttpTextFormat.Getter<Metadata>() {
        @Override
        public String get(Metadata carrier, String key) {
            Metadata.Key<String> k = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
            if(carrier.containsKey(k)){
                return carrier.get(k);
            }
            return "";
        }
    };

    public HelloWorldServer(){
        initTracer();
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                // Intercept gRPC calls
                .intercept(new OpenTelemetryServerInterceptor())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                HelloWorldServer.this.stop();
                System.err.println("*** server shut down");
                System.err.println("*** forcing also the Tracer Exporter to shutdown and process the remaining traces");
                inMemexporter.shutdown();
                System.err.println("*** Trace Exporter shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void initTracer(){
        // Get the tracer
        TracerSdkFactory tracer = OpenTelemetrySdk.getTracerFactory();
        // Set to process in memory the spans
        tracer.addSpanProcessor(
                SimpleSpansProcessor.newBuilder(inMemexporter).build()
        );
        // Give the name to the traces
        OTel = tracer.get("example/grpc");
        textFormat = OTel.getHttpTextFormat();
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

     class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            // Extract the Context from the gRPC request
            SpanContext ctx = TRACE_ID_CTX_KEY.get();
            // Build a span based on the received context
            Span span = OTel
                    .spanBuilder("hello handler")
                    .setParent(ctx)
                    .startSpan();

            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            // Terminate the span
            span.end();
        }
    }

    private class OpenTelemetryServerInterceptor implements io.grpc.ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            // Extract the Span Context from the metadata of the gRPC request
            SpanContext sctx = textFormat.extract(headers, getter);
            // Set it as value of the current gRPC context
            Context ctx = Context.current().withValue(TRACE_ID_CTX_KEY, sctx);
            // Process the gRPC call normally
            return Contexts.interceptCall(ctx, call, headers, next);
        }
    }


    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();

        // Print new traces every 1s
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        for (SpanData spanData : server.inMemexporter.getFinishedSpanItems()) {
                            System.out.println("  - " + spanData);
                        }
                        server.inMemexporter.reset();
                    } catch (Exception e) {
                    }
                }
            }
        };
        t.start();

        server.blockUntilShutdown();
    }
}
