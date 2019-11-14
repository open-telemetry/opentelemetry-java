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

package io.opentelemetry.contrib.http.servlet;

import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletUtils.CONTENT_LENGTH;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletUtils.OTEL_SERVLET_LISTENER;
import static org.junit.Assert.assertFalse;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.http.core.HttpExtractor;
import io.opentelemetry.contrib.http.core.HttpRequestContext;
import io.opentelemetry.contrib.http.core.HttpServerHandler;
import io.opentelemetry.contrib.http.core.HttpStatus2OtStatusConverter;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Tracer;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

/** Unit tests for {@link OtelHttpServletListener}. */
public class OtelHttpServletListenerTest {

  private static final Logger LOGGER =
      Logger.getLogger(OtelHttpServletListenerTest.class.getName());
  private static InMemoryTracing inMemoryTracing;

  @BeforeClass
  public static void configureTracing() {
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
    TracerSdkFactory tracerSdk = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
    tracerSdk.updateActiveTraceConfig(traceConfig);
    inMemoryTracing = new InMemoryTracing(tracerSdk);
  }

  private Tracer tracer;
  private HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler;

  @Before
  public void setUp() {
    tracer = OpenTelemetry.getTracerFactory().get(INSTRUMENTATION_LIB_ID);
    HttpStatus2OtStatusConverter statusConverter = new HttpStatus2OtStatusConverter();
    HttpExtractor<HttpServletRequest, HttpServletResponse> httpExtractor =
        new UriPathDrivenHttpServletExtractor();
    MultiSchemeHttpPropagationGetter getter =
        new MultiSchemeHttpPropagationGetter(W3cTraceContextHttpPropagationGetter.SCHEME_NAME);
    getter.initializeGetters();
    handler =
        new HttpServerHandler<>(
            httpExtractor,
            getter,
            statusConverter,
            tracer,
            OpenTelemetry.getDistributedContextManager(),
            OpenTelemetry.getMeterFactory().get(INSTRUMENTATION_LIB_ID),
            true);
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
  public void shouldRecordTracesAndMetricsOnSuccessfulAsychronousRequest() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "https://api.example.org/users");
    request.addHeader(CONTENT_LENGTH, "1024");
    request.setSession(new MockHttpSession());
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.addHeader(CONTENT_LENGTH, "3072");
    HttpRequestContext context = handler.handleStart(request, request);
    OtelHttpServletListener listener = new OtelHttpServletListener(tracer, handler, context);
    request.setAttribute(OTEL_SERVLET_LISTENER, listener);

    try (Scope scope = OtelHttpServletUtils.withScope(request)) {
      MockAsyncContext asyncContext = new MockAsyncContext(request, response);
      AsyncEvent startEvent = new AsyncEvent(asyncContext);
      listener.onStartAsync(startEvent);
      AsyncEvent endEvent = new AsyncEvent(asyncContext);
      listener.onComplete(endEvent);
    }

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldRecordTracesAndMetricsOnFailedAsychronousRequest() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "https://api.example.org/users");
    request.addHeader(CONTENT_LENGTH, "1024");
    request.setSession(new MockHttpSession());
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.addHeader(CONTENT_LENGTH, "3072");
    HttpRequestContext context = handler.handleStart(request, request);
    OtelHttpServletListener listener = new OtelHttpServletListener(tracer, handler, context);
    request.setAttribute(OTEL_SERVLET_LISTENER, listener);

    try (Scope scope = OtelHttpServletUtils.withScope(request)) {
      MockAsyncContext asyncContext = new MockAsyncContext(request, response);
      AsyncEvent startEvent = new AsyncEvent(asyncContext);
      listener.onStartAsync(startEvent);
      AsyncEvent endEvent = new AsyncEvent(asyncContext, new ServletException("this is a test"));
      listener.onError(endEvent);
    }

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldRecordTracesAndMetricsOnTimedOutAsychronousRequest() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "https://api.example.org/users");
    request.addHeader(CONTENT_LENGTH, "1024");
    request.setSession(new MockHttpSession());
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.addHeader(CONTENT_LENGTH, "3072");
    HttpRequestContext context = handler.handleStart(request, request);
    OtelHttpServletListener listener = new OtelHttpServletListener(tracer, handler, context);
    request.setAttribute(OTEL_SERVLET_LISTENER, listener);

    try (Scope scope = OtelHttpServletUtils.withScope(request)) {
      MockAsyncContext asyncContext = new MockAsyncContext(request, response);
      AsyncEvent startEvent = new AsyncEvent(asyncContext);
      listener.onStartAsync(startEvent);
      AsyncEvent endEvent =
          new AsyncEvent(asyncContext, new SocketTimeoutException("this is a test"));
      listener.onTimeout(endEvent);
    }

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }
}
