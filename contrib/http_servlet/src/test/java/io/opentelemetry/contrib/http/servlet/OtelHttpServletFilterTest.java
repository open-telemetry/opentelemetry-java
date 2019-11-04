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

import static com.google.common.base.Strings.lenientFormat;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.ALL_SCHEMES;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletFilter.OTEL_EXTRACTOR;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletFilter.OTEL_PATH_EXCLUDES;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletFilter.OTEL_PATH_INCLUDES;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletFilter.OTEL_PUBLIC_ENDPOINT;
import static io.opentelemetry.contrib.http.servlet.OtelHttpServletFilter.OTEL_TRACE_PROPAGATORS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/** Unit tests for {@link OtelHttpServletFilter}. */
public class OtelHttpServletFilterTest {

  private static final Logger LOGGER = Logger.getLogger(OtelHttpServletFilterTest.class.getName());
  private static TracerSdk tracerSdk;
  private static InMemoryTracing inMemoryTracing;

  @BeforeClass
  public static void configureTracing() {
    TraceConfig traceConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
    TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
    tracerSdkFactory.updateActiveTraceConfig(traceConfig);
    inMemoryTracing = new InMemoryTracing(tracerSdkFactory);
    tracerSdk = tracerSdkFactory.get(INSTRUMENTATION_LIB_ID);
  }

  @Rule public final ExpectedException exception = ExpectedException.none();
  private OtelHttpServletFilter filter;

  @Before
  public void setUp() throws ServletException {
    filter = new OtelHttpServletFilter();
    FilterConfig filterConfig = mock(FilterConfig.class);
    when(filterConfig.getInitParameter(OTEL_TRACE_PROPAGATORS)).thenReturn(ALL_SCHEMES);
    when(filterConfig.getInitParameter(OTEL_EXTRACTOR)).thenReturn("NONE");
    when(filterConfig.getInitParameter(OTEL_PATH_INCLUDES)).thenReturn("/**");
    when(filterConfig.getInitParameter(OTEL_PATH_EXCLUDES)).thenReturn("/actuator/**");
    when(filterConfig.getInitParameter(OTEL_PUBLIC_ENDPOINT)).thenReturn("false");
    MockServletContext servletContext = new MockServletContext();
    when(filterConfig.getServletContext()).thenReturn(servletContext);
    filter.init(filterConfig);
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
  public void shouldContinueTraceAndStartNewSpanOnSyncRequest()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    final TraceId traceId = TraceId.fromLowerBase16("36f18e863cca4fbabfffbc1ef699f7de", 0);
    final SpanId remoteSpanId = SpanId.fromLowerBase16("30cf9ea8c33d4581", 0);
    String traceparent =
        "00-" + traceId.toLowerBase16() + "-" + remoteSpanId.toLowerBase16() + "-01";
    request.addHeader(TRACEPARENT, traceparent);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain =
        new FilterChain() {
          @Override
          public void doFilter(ServletRequest request, ServletResponse response) {
            Span span = tracerSdk.getCurrentSpan();
            assertEquals(traceId, span.getContext().getTraceId());
            assertFalse(remoteSpanId.equals(span.getContext().getSpanId()));
            assertTrue(span.isRecording());
            LOGGER.info(lenientFormat("continuing filter chain with %s", span.getContext()));
          }
        };
    filter.doFilter(request, response, filterChain);

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldContinueTraceAndStartNewSpanOnAsyncRequest()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    request.setAsyncSupported(true);
    final TraceId traceId = TraceId.fromLowerBase16("36f18e863cca4fbabfffbc1ef699f7de", 0);
    final SpanId remoteSpanId = SpanId.fromLowerBase16("30cf9ea8c33d4581", 0);
    String traceparent =
        "00-" + traceId.toLowerBase16() + "-" + remoteSpanId.toLowerBase16() + "-01";
    request.addHeader(TRACEPARENT, traceparent);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AsyncContext asyncContext = request.startAsync(request, response);
    FilterChain filterChain =
        new FilterChain() {
          @Override
          public void doFilter(ServletRequest request, ServletResponse response) {
            Span span = tracerSdk.getCurrentSpan();
            assertEquals(traceId, span.getContext().getTraceId());
            assertFalse(remoteSpanId.equals(span.getContext().getSpanId()));
            assertTrue(span.isRecording());
            LOGGER.info(lenientFormat("continuing filter chain with %s", span.getContext()));
          }
        };
    filter.doFilter(request, response, filterChain);
    asyncContext.complete();

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldPassThruNonHttpRequests() throws IOException, ServletException {
    ServletRequest request = mock(ServletRequest.class);
    ServletResponse response = mock(ServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);
    filter.doFilter(request, response, filterChain);
    verify(filterChain).doFilter(request, response);

    assertTrue(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldPassThruExcludedPathHttpRequests() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    filter.doFilter(request, response, filterChain);
    verify(filterChain).doFilter(request, response);

    assertTrue(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldGenerateNewTraceIfNoTracingHeadersReceived()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain =
        new FilterChain() {
          @Override
          public void doFilter(ServletRequest request, ServletResponse response) {
            Span span = tracerSdk.getCurrentSpan();
            assertTrue(span.getContext().isValid());
            LOGGER.info(lenientFormat("continuing filter chain with %s", span.getContext()));
          }
        };
    filter.doFilter(request, response, filterChain);

    assertFalse(inMemoryTracing.getFinishedSpanItems().isEmpty());
  }

  @Test
  public void shouldRecordTelemetryOnException() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
    final TraceId traceId = TraceId.fromLowerBase16("36f18e863cca4fbabfffbc1ef699f7de", 0);
    final SpanId remoteSpanId = SpanId.fromLowerBase16("30cf9ea8c33d4581", 0);
    String traceparent =
        "00-" + traceId.toLowerBase16() + "-" + remoteSpanId.toLowerBase16() + "-01";
    request.addHeader(TRACEPARENT, traceparent);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain =
        new FilterChain() {
          @Override
          public void doFilter(ServletRequest request, ServletResponse response)
              throws ServletException {
            Span span = tracerSdk.getCurrentSpan();
            assertEquals(traceId, span.getContext().getTraceId());
            assertFalse(remoteSpanId.equals(span.getContext().getSpanId()));
            assertTrue(span.isRecording());
            LOGGER.info(lenientFormat("continuing filter chain with %s", span.getContext()));
            throw new ServletException("this is a test");
          }
        };
    exception.expect(ServletException.class);
    filter.doFilter(request, response, filterChain);
  }
}
