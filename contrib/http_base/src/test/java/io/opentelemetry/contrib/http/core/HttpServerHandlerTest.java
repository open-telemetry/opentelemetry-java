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

package io.opentelemetry.contrib.http.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Unit tests for {@link HttpServerHandler}. */
public class HttpServerHandlerTest {

  private static final Logger LOGGER = Logger.getLogger(HttpServerHandlerTest.class.getName());
  private static InMemoryTracing inMemoryTracing;

  @BeforeClass
  public static void configureTracing() {
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
    TracerSdk tracerSdk = (TracerSdk) OpenTelemetry.getTracer();
    tracerSdk.updateActiveTraceConfig(traceConfig);
    inMemoryTracing = new InMemoryTracing(tracerSdk);
  }

  @Before
  public void reset() {
    if (inMemoryTracing != null) {
      inMemoryTracing.reset();
    }
  }

  @After
  public void printSpans() {
    for (SpanData span : inMemoryTracing.getFinishedSpanItems()) {
      LOGGER.log(Level.FINE, span.toString());
    }
  }

  @Test
  public void shouldStartAndEndSpanWhenRemoteDataIsAvailable() {
    Map<String, String> data = new HashMap<>();
    String traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
    TraceId traceId = TraceId.fromLowerBase16(traceParent, 3);
    SpanId spanId = SpanId.fromLowerBase16(traceParent, 36);
    data.put("traceparent", traceParent);
    data.put("tracestate", "congo=t61rcWkgMzE");
    data.put(TestOnlyMapHttpExtractor.METHOD, "GET");
    String url = "https://api.example.com/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC";
    data.put(TestOnlyMapHttpExtractor.URL, url);
    data.put(TestOnlyMapHttpExtractor.PATH, "/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC");
    data.put(TestOnlyMapHttpExtractor.ROUTE, "/widgets/{id}");
    data.put(TestOnlyMapHttpExtractor.STATUS, "200");
    HttpServerHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpServerHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    HttpRequestContext context = handler.handleStart(data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    try {
      assertEquals(traceId, currentSpan.getContext().getTraceId());
      assertFalse(spanId.equals(currentSpan.getContext().getSpanId()));
      assertTrue(currentSpan.isRecording());
    } finally {
      handler.handleEnd(context, data, data, null);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldStartAndEndSpanWhenRemoteDataIsAvailableButIsPublicEndpoint() {
    Map<String, String> data = new HashMap<>();
    String traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
    TraceId traceId = TraceId.fromLowerBase16(traceParent, 3);
    SpanId spanId = SpanId.fromLowerBase16(traceParent, 36);
    data.put("traceparent", traceParent);
    data.put("tracestate", "congo=t61rcWkgMzE");
    data.put(TestOnlyMapHttpExtractor.METHOD, "GET");
    String url = "https://api.example.com/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC";
    data.put(TestOnlyMapHttpExtractor.URL, url);
    data.put(TestOnlyMapHttpExtractor.PATH, "/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC");
    data.put(TestOnlyMapHttpExtractor.ROUTE, "/widgets/{id}");
    data.put(TestOnlyMapHttpExtractor.STATUS, "200");
    HttpServerHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpServerHandler<>(
            new TestOnlyMapHttpExtractor(),
            new TestOnlyMapGetterSetter(),
            new HttpStatus2OtStatusConverter(),
            OpenTelemetry.getTracer(),
            OpenTelemetry.getDistributedContextManager(),
            OpenTelemetry.getMeter(),
            Boolean.TRUE);
    HttpRequestContext context = handler.handleStart(data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    try {
      assertFalse(traceId.equals(currentSpan.getContext().getTraceId()));
      assertFalse(spanId.equals(currentSpan.getContext().getSpanId()));
    } finally {
      handler.handleEnd(context, data, data, null);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldStartAndEndSpanWhenRemoteDataIsNotAvailable() {
    Map<String, String> data = new HashMap<>();
    data.put(TestOnlyMapHttpExtractor.METHOD, "GET");
    String url = "https://api.example.com/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC";
    data.put(TestOnlyMapHttpExtractor.URL, url);
    data.put(TestOnlyMapHttpExtractor.PATH, "/widgets/6544557E-820C-4975-9E2A-C959B3F7D2FC");
    data.put(TestOnlyMapHttpExtractor.ROUTE, "/widgets/{id}");
    data.put(TestOnlyMapHttpExtractor.STATUS, "200");
    HttpServerHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpServerHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    HttpRequestContext context = handler.handleStart(data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    try {
      assertTrue(currentSpan.getContext().isValid());
    } finally {
      handler.handleEnd(context, data, data, null);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldAddExceptionInfoToSpanIfProvided() {
    Map<String, String> data = new HashMap<>();
    String traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
    TraceId traceId = TraceId.fromLowerBase16(traceParent, 3);
    SpanId spanId = SpanId.fromLowerBase16(traceParent, 36);
    data.put("traceparent", traceParent);
    data.put("tracestate", "congo=t61rcWkgMzE");
    data.put(TestOnlyMapHttpExtractor.METHOD, "POST");
    String url = "https://api.example.com/widgets";
    data.put(TestOnlyMapHttpExtractor.URL, url);
    data.put(TestOnlyMapHttpExtractor.PATH, "/widgets");
    data.put(TestOnlyMapHttpExtractor.ROUTE, "/widgets");
    data.put(TestOnlyMapHttpExtractor.STATUS, "409");
    HttpServerHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpServerHandler<>(
            new TestOnlyMapHttpExtractor(),
            new TestOnlyMapGetterSetter(),
            new ExtendedHttpStatus2OtStatusConverter(),
            OpenTelemetry.getTracer(),
            OpenTelemetry.getDistributedContextManager(),
            OpenTelemetry.getMeter(),
            Boolean.FALSE);
    HttpRequestContext context = handler.handleStart(data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    IllegalStateException error = new IllegalStateException("this is a test");
    try {
      assertEquals(traceId, currentSpan.getContext().getTraceId());
      assertFalse(spanId.equals(currentSpan.getContext().getSpanId()));
    } finally {
      handler.handleEnd(context, data, data, error);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }
}
