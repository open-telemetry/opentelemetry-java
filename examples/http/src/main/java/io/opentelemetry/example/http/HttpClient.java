/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.http;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class HttpClient {

  // OTel API
  private static final Tracer tracer =
      OpenTelemetry.getGlobalTracer("io.opentelemetry.example.http.HttpClient");
  // Export traces to log
  private static final LoggingSpanExporter loggingExporter = new LoggingSpanExporter();
  // Inject the span context into the request
  private static final TextMapPropagator.Setter<HttpURLConnection> setter =
      URLConnection::setRequestProperty;

  private static void initTracing() {
    // install the W3C Trace Context propagator
    OpenTelemetry.setGlobalPropagators(
        DefaultContextPropagators.builder()
            .addTextMapPropagator(HttpTraceContext.getInstance())
            .build());

    // Get the tracer management instance.
    TracerSdkManagement tracerManagement = OpenTelemetrySdk.getGlobalTracerManagement();
    // Show that multiple exporters can be used

    // Set to export the traces also to a log file
    tracerManagement.addSpanProcessor(SimpleSpanProcessor.builder(loggingExporter).build());
  }

  private void makeRequest() throws IOException {
    int port = 8080;
    URL url = new URL("http://127.0.0.1:" + port);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    int status = 0;
    StringBuilder content = new StringBuilder();

    // Name convention for the Span is not yet defined.
    // See: https://github.com/open-telemetry/opentelemetry-specification/issues/270
    Span span = tracer.spanBuilder("/").setSpanKind(Span.Kind.CLIENT).startSpan();
    try (Scope scope = span.makeCurrent()) {
      // TODO provide semantic convention attributes to Span.Builder
      span.setAttribute("component", "http");
      span.setAttribute("http.method", "GET");
      /*
       Only one of the following is required:
         - http.url
         - http.scheme, http.host, http.target
         - http.scheme, peer.hostname, peer.port, http.target
         - http.scheme, peer.ip, peer.port, http.target
      */
      span.setAttribute("http.url", url.toString());

      // Inject the request with the current Context/Span.
      OpenTelemetry.getGlobalPropagators()
          .getTextMapPropagator()
          .inject(Context.current(), con, setter);

      try {
        // Process the request
        con.setRequestMethod("GET");
        status = con.getResponseCode();
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(con.getInputStream(), Charset.defaultCharset()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          content.append(inputLine);
        }
        in.close();
      } catch (Exception e) {
        span.setStatus(StatusCode.ERROR, "HTTP Code: " + status);
      }
    } finally {
      span.end();
    }

    // Output the result of the request
    System.out.println("Response Code: " + status);
    System.out.println("Response Msg: " + content);
  }

  /**
   * Main method to run the example.
   *
   * @param args It is not required.
   */
  public static void main(String[] args) {
    initTracing();
    HttpClient httpClient = new HttpClient();

    // Perform request every 5s
    Thread t =
        new Thread(
            () -> {
              while (true) {
                try {
                  httpClient.makeRequest();
                  Thread.sleep(5000);
                } catch (Exception e) {
                  System.out.println(e.getMessage());
                }
              }
            });
    t.start();
  }
}
