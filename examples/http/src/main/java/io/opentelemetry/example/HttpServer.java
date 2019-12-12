/*
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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.inmemory.InMemorySpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.*;
import io.opentelemetry.trace.propagation.HttpTraceContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

  private class HelloHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
      // Name convention for the Span is not yet defined.
      // See: https://github.com/open-telemetry/opentelemetry-specification/issues/270
      Span.Builder spanBuilder = tracer.spanBuilder("/").setSpanKind(Span.Kind.SERVER);
      Span span = null;
      try {
        // Extract the context from the HTTP request
        SpanContext ctx = tracer.getHttpTextFormat().extract(he, getter);
        // Rebuild a span with the received context
        span = spanBuilder.setParent(ctx).startSpan();
      } catch (StringIndexOutOfBoundsException e) {
        // msg without ctx
        span = spanBuilder.startSpan();
      }
      // Set the Semantic Convention
      span.setAttribute("component", "http");
      span.setAttribute("http.method", "GET");
      /*
      One of the following is required:
      - http.scheme, http.host, http.target
      - http.scheme, http.server_name, net.host.port, http.target
      - http.scheme, net.host.name, net.host.port, http.target
      - http.url
      */
      span.setAttribute("http.scheme", "http");
      span.setAttribute("http.host", "localhost:" + HttpServer.port);
      span.setAttribute("http.target", "/");
      // Process the request
      answer(he, span);
      // Close the span
      span.end();
    }

    private void answer(HttpExchange he, Span span) throws IOException {
      // Generate an Event
      span.addEvent("Start Processing");

      // Process the request
      String response = "Hello World!";
      he.sendResponseHeaders(200, response.length());
      OutputStream os = he.getResponseBody();
      os.write(response.getBytes());
      os.close();
      System.out.println("Served Client: " + he.getRemoteAddress());

      // Generate an Event with an attribute
      Map<String, AttributeValue> event = new HashMap<>();
      event.put("answer", AttributeValue.stringAttributeValue(response));
      span.addEvent("Finish Processing", event);
      
      // Everything works fine in this example
      span.setStatus(Status.OK);
    }
  }

  com.sun.net.httpserver.HttpServer server;
  static int port = 8080;

  // OTel API
  Tracer tracer;
  // Export traces in memory
  InMemorySpanExporter inMemexporter = InMemorySpanExporter.create();
  // Extract the context from http headers
  HttpTextFormat.Getter<HttpExchange> getter =
      new HttpTextFormat.Getter<HttpExchange>() {
        @Override
        public String get(HttpExchange carrier, String key) {
          if (carrier.getRequestHeaders().containsKey(key)) {
            return carrier.getRequestHeaders().get(key).get(0);
          }
          return "";
        }
      };

  public HttpServer() throws IOException {
    this(port);
  }

  public HttpServer(int port) throws IOException {
    initTracer();
    server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
    // Test urls
    server.createContext("/", new HelloHandler());
    server.start();
    System.out.println("Server ready on http://127.0.0.1:" + port);
  }

  private void initTracer() {
    // Get the tracer
    TracerSdkFactory tracerFactory = OpenTelemetrySdk.getTracerFactory();
    // Set to process in memory the spans
    tracerFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(inMemexporter).build());
    // Give a name to the traces
    this.tracer = tracerFactory.get("io.opentelemetry.example.HttpServer");
  }

  private void stop() {
    server.stop(0);
  }

  public static void main(String[] args) throws Exception {
    final HttpServer s = new HttpServer();
    // Gracefully close the server
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                s.stop();
              }
            });
    // Print new traces every 1s
    Thread t =
        new Thread() {
          @Override
          public void run() {
            while (true) {
              try {
                Thread.sleep(1000);
                for (SpanData spanData : s.inMemexporter.getFinishedSpanItems()) {
                  System.out.println("  - " + spanData);
                }
                s.inMemexporter.reset();
              } catch (Exception e) {
              }
            }
          }
        };
    t.start();
  }
}
