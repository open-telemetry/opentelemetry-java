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

package io.opentelemetry.example.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
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

      // Extract the context from the HTTP request
      Context ctx =
          OpenTelemetry.getPropagators().getHttpTextFormat().extract(Context.current(), he, getter);
      try (Scope scope = ContextUtils.withScopedContext(ctx)) {
        // Build a span automatically using the received context
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
      os.write(response.getBytes(Charset.defaultCharset()));
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

  private com.sun.net.httpserver.HttpServer server;
  private static int port = 8080;

  // OTel API
  private static Tracer tracer =
      OpenTelemetry.getTracerProvider().get("io.opentelemetry.example.http.HttpServer");
  // Export traces to log
  private static LoggingSpanExporter loggingExporter = new LoggingSpanExporter();
  // Extract the context from http headers
  private static HttpTextFormat.Getter<HttpExchange> getter =
      new HttpTextFormat.Getter<HttpExchange>() {
        @Override
        public String get(HttpExchange carrier, String key) {
          if (carrier.getRequestHeaders().containsKey(key)) {
            return carrier.getRequestHeaders().get(key).get(0);
          }
          return "";
        }
      };

  private HttpServer() throws IOException {
    this(port);
  }

  private HttpServer(int port) throws IOException {
    initTracer();
    server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
    // Test urls
    server.createContext("/", new HelloHandler());
    server.start();
    System.out.println("Server ready on http://127.0.0.1:" + port);
  }

  private void initTracer() {
    // Get the tracer
    TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
    // Show that multiple exporters can be used

    // Set to export the traces also to a log file
    tracerProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(loggingExporter).build());
  }

  private void stop() {
    server.stop(0);
  }

  /**
   * Main method to run the example.
   *
   * @param args It is not required.
   * @throws Exception Something might go wrong.
   */
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
  }
}
