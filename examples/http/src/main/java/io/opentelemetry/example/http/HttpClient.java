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

import io.grpc.Context;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HttpClient {

  // OTel API
  private static Tracer tracer =
      OpenTelemetry.getTracerProvider().get("io.opentelemetry.example.http.HttpClient");
  // Export traces to log
  private static LoggingSpanExporter loggingExporter = new LoggingSpanExporter();
  // Inject the span context into the request
  private static HttpTextFormat.Setter<HttpURLConnection> setter =
      new HttpTextFormat.Setter<HttpURLConnection>() {
        @Override
        public void set(HttpURLConnection carrier, String key, String value) {
          carrier.setRequestProperty(key, value);
        }
      };

  private void initTracer() {
    // Get the tracer
    TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
    // Show that multiple exporters can be used

    // Set to export the traces also to a log file
    tracerProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(loggingExporter).build());
  }

  private HttpClient() throws Exception {
    initTracer();

    // Connect to the server locally
    int port = 8080;
    URL url = new URL("http://127.0.0.1:" + port);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    int status = 0;
    StringBuilder content = new StringBuilder();

    // Name convention for the Span is not yet defined.
    // See: https://github.com/open-telemetry/opentelemetry-specification/issues/270
    Span span = tracer.spanBuilder("/").setSpanKind(Span.Kind.CLIENT).startSpan();
    try (Scope scope = tracer.withSpan(span)) {
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
      OpenTelemetry.getPropagators().getHttpTextFormat().inject(Context.current(), con, setter);

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
        // Close the Span
        span.setStatus(Status.OK);
      } catch (Exception e) {
        // TODO create mapping for Http Error Codes <-> io.opentelemetry.trace.Status
        span.setStatus(Status.UNKNOWN.withDescription("HTTP Code: " + status));
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
    // Perform request every 5s
    Thread t =
        new Thread() {
          @Override
          public void run() {
            while (true) {
              try {
                HttpClient c = new HttpClient();
                Thread.sleep(5000);
              } catch (Exception e) {
                System.out.println(e.getMessage());
              }
            }
          }
        };
    t.start();
  }
}
