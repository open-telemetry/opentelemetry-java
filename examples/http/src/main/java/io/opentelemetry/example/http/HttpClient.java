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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.exporters.inmemory.InMemorySpanExporter;
import io.opentelemetry.exporters.logging.LoggingExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
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

  static int port = 8080;
  // OTel API
  Tracer tracer = OpenTelemetry.getTracerFactory().get("io.opentelemetry.example.http.HttpClient");
  // Export traces in memory
  InMemorySpanExporter inMemexporter = InMemorySpanExporter.create();
  // Export traces to log
  LoggingExporter loggingExporter = new LoggingExporter();
  // Inject the span context into the request
  HttpTextFormat.Setter<HttpURLConnection> setter =
      new HttpTextFormat.Setter<HttpURLConnection>() {
        @Override
        public void put(HttpURLConnection carrier, String key, String value) {
          carrier.setRequestProperty(key, value);
        }
      };

  private void initTracer() {
    // Get the tracer
    TracerSdkFactory tracerFactory = OpenTelemetrySdk.getTracerFactory();
    // Show that multiple exporters can be used

    // Set to process in memory the spans
    tracerFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(inMemexporter).build());
    // Set to export the traces also to a log file
    tracerFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(loggingExporter).build());
  }

  private HttpClient() throws Exception {
    initTracer();

    // Connect to the server locally
    URL url = new URL("http://127.0.0.1:" + port);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    // Name convention for the Span is not yet defined.
    // See: https://github.com/open-telemetry/opentelemetry-specification/issues/270
    Span span = tracer.spanBuilder("/").setSpanKind(Span.Kind.CLIENT).startSpan();
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

    // Inject the request with the context
    tracer.getHttpTextFormat().inject(span.getContext(), con, setter);

    StringBuilder content = new StringBuilder();
    int status = 0;
    try {
      // Process the request
      con.setRequestMethod("GET");
      status = con.getResponseCode();
      BufferedReader in =
          new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.defaultCharset()));
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
    span.end();

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
                for (SpanData spanData : c.inMemexporter.getFinishedSpanItems()) {
                  System.out.println("  - " + spanData);
                }
                c.inMemexporter.reset();
              } catch (Exception e) {
                System.out.println(e.getMessage());
              }
            }
          }
        };
    t.start();
  }
}
