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
import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryMetadata.EntryTtl;
import io.opentelemetry.distributedcontext.EntryValue;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Unit tests for {@link HttpClientHandler}. */
public class HttpClientHandlerTest {

  private static final Logger LOGGER = Logger.getLogger(HttpClientHandlerTest.class.getName());
  private static InMemoryTracing inMemoryTracing;

  @BeforeClass
  public static void configureTracing() {
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
    TracerSdk tracerSdk = (TracerSdk) OpenTelemetry.getTracer();
    LOGGER.log(Level.INFO, "Orignal trace config: " + tracerSdk.getActiveTraceConfig());
    tracerSdk.updateActiveTraceConfig(traceConfig);
    LOGGER.log(Level.INFO, "Updated trace config: " + tracerSdk.getActiveTraceConfig());
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
  public void shouldStartAndEndSpanWhenParentIsAvailable() {
    Map<String, String> data = new HashMap<>();
    HttpClientHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpClientHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    Span parentSpan = constructParentSpan();
    DistributedContext parentContext = constructParentContext();
    LOGGER.log(Level.INFO, parentSpan.getContext().toString());
    try (Scope parent = OpenTelemetry.getTracer().withSpan(parentSpan);
        Scope contextScope =
            OpenTelemetry.getDistributedContextManager().withContext(parentContext)) {
      HttpRequestContext context = handler.handleStart(null, null, data, data);
      Span currentSpan = handler.getSpanFromContext(context);
      LOGGER.log(Level.INFO, currentSpan.getContext().toString());
      try {
        assertEquals(parentSpan.getContext().getTraceId(), currentSpan.getContext().getTraceId());
        assertFalse(
            parentSpan.getContext().getSpanId().equals(currentSpan.getContext().getSpanId()));
        assertTrue(currentSpan.isRecording());
      } finally {
        handler.handleEnd(context, data, data, null);
      }
      assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
      printSpans();
    }
  }

  @Test
  public void shouldStartAndEndSpanWithProvidedParent() {
    Map<String, String> data = new HashMap<>();
    HttpClientHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpClientHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    Span parentSpan = constructParentSpan();
    DistributedContext parentContext = constructParentContext();
    LOGGER.log(Level.INFO, parentSpan.getContext().toString());
    HttpRequestContext context = handler.handleStart(parentSpan, parentContext, data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    LOGGER.log(Level.INFO, currentSpan.getContext().toString());
    try {
      assertEquals(parentSpan.getContext().getTraceId(), currentSpan.getContext().getTraceId());
      assertFalse(parentSpan.getContext().getSpanId().equals(currentSpan.getContext().getSpanId()));
      assertTrue(currentSpan.isRecording());
    } finally {
      handler.handleEnd(context, data, data, null);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
    printSpans();
  }

  @Test
  public void shouldStartAndEndSpanWithNoParent() {
    Map<String, String> data = new HashMap<>();
    HttpClientHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpClientHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    HttpRequestContext context = handler.handleStart(null, null, data, data);
    Span currentSpan = handler.getSpanFromContext(context);
    LOGGER.log(Level.INFO, currentSpan.getContext().toString());
    try {
      assertTrue(currentSpan.getContext().isValid());
      assertTrue(currentSpan.isRecording());
    } finally {
      handler.handleEnd(context, data, data, null);
    }
    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
    printSpans();
  }

  @Test
  public void shouldAddExceptionInfoToSpanIfProvided() {
    Map<String, String> data = new HashMap<>();
    HttpClientHandler<Map<String, String>, Map<String, String>, Map<String, String>> handler =
        new HttpClientHandler<>(new TestOnlyMapHttpExtractor(), new TestOnlyMapGetterSetter());
    Span parentSpan = constructParentSpan();
    DistributedContext parentContext = constructParentContext();
    LOGGER.log(Level.INFO, parentSpan.getContext().toString());
    try (Scope parent = OpenTelemetry.getTracer().withSpan(parentSpan);
        Scope contextScope =
            OpenTelemetry.getDistributedContextManager().withContext(parentContext)) {
      HttpRequestContext context = handler.handleStart(null, null, data, data);
      Span currentSpan = handler.getSpanFromContext(context);
      LOGGER.log(Level.INFO, currentSpan.getContext().toString());
      IllegalStateException error = new IllegalStateException("this is a test");
      try {
        assertEquals(parentSpan.getContext().getTraceId(), currentSpan.getContext().getTraceId());
        assertFalse(
            parentSpan.getContext().getSpanId().equals(currentSpan.getContext().getSpanId()));
        assertTrue(currentSpan.isRecording());
      } finally {
        handler.handleEnd(context, data, data, error);
      }
      assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
      printSpans();
    }
  }

  private static Span constructParentSpan() {
    return OpenTelemetry.getTracer()
        .spanBuilder("/junit")
        .setNoParent()
        .setSampler(Samplers.alwaysOn())
        .setSpanKind(Kind.SERVER)
        .startSpan();
  }

  private static DistributedContext constructParentContext() {
    return OpenTelemetry.getDistributedContextManager()
        .contextBuilder()
        .put(
            EntryKey.create("testClass"),
            EntryValue.create("HttpClientHandlerTest"),
            EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION))
        .build();
  }
}
