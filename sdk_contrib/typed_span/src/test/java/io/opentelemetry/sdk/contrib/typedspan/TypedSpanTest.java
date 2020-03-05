/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.contrib.typedspan;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemorySpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.contrib.typedspan.http.HttpClientSpanWrapper;
import io.opentelemetry.sdk.contrib.typedspan.http.HttpClientSpanWrapperBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opentelemetry.sdk.contrib.typedspan.http.HttpClientSpanWrapper}. */
@RunWith(JUnit4.class)
public class TypedSpanTest {

  InMemorySpanExporter exporter = InMemorySpanExporter.create();
  Tracer tracer;

  @Before
  public void setUp() throws Exception {
    OpenTelemetrySdk.getTracerRegistry()
        .addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
    tracer = OpenTelemetry.getTracerProvider().get(this.getClass().getCanonicalName());
  }

  @Test
  public void testURLConnectio() throws Exception {
    URLConnection connection = new URL("https://www.google.com/q?=typed%20span").openConnection();
    HttpClientSpanWrapper http_span =
        HttpClientSpanWrapperBuilder.create(tracer, connection).startSpan();
    try {
      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()));
      String line;
      while ((line = bufferedReader.readLine()) != null) ;
      bufferedReader.close();
    } catch (Exception e) {
      // the outcome of the HTTP request is not relevant for the test
    }
    http_span.end(Status.OK);

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans.size()).isEqualTo(1);
    SpanData span = spans.get(0);
    // Empty fields should not be dropped
    assertThat(span.getAttributes().size()).isEqualTo(4);
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.URL_KEY).getStringValue())
        .isEqualTo("https://www.google.com/q?=typed%20span");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.HOST_KEY).getStringValue())
        .isEqualTo("");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.SCHEME_KEY).getStringValue())
        .isEqualTo("https");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.METHOD_KEY).getStringValue())
        .isEqualTo("GET");
  }

  @Test
  public void testHttpURLConnectio() throws Exception {
    HttpURLConnection connection =
        (HttpURLConnection) new URL("https://www.google.com/q?=typed%20span").openConnection();
    HttpClientSpanWrapper http_span =
        HttpClientSpanWrapperBuilder.create(tracer, connection).startSpan();
    try {
      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()));
      String line;
      while ((line = bufferedReader.readLine()) != null) ;
      bufferedReader.close();
    } catch (Exception e) {
      // the outcome of the HTTP request is not relevant for the test
    }
    http_span.end(Status.OK);

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans.size()).isEqualTo(1);
    SpanData span = spans.get(0);
    // Empty fields should not be dropped
    assertThat(span.getAttributes().size()).isEqualTo(4);
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.URL_KEY).getStringValue())
        .isEqualTo("https://www.google.com/q?=typed%20span");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.HOST_KEY).getStringValue())
        .isEqualTo("");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.SCHEME_KEY).getStringValue())
        .isEqualTo("https");
    assertThat(span.getAttributes().get(HttpClientSpanWrapperBuilder.METHOD_KEY).getStringValue())
        .isEqualTo("GET");
  }
}
