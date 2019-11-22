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

package io.opentelemetry.contrib.http.jaxrs;

import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.contrib.http.core.HttpClientHandler;
import io.opentelemetry.contrib.http.core.HttpExtractor;
import io.opentelemetry.contrib.http.core.HttpRequestContext;
import io.opentelemetry.contrib.http.core.HttpStatus2OtStatusConverter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Implementation of JAX-RS client request and response filter instruments client calls with
 * OpenTelemetry tracing and metrics.
 */
@Provider
public class OtelJaxrsClientFilter implements ClientRequestFilter, ClientResponseFilter {

  private static final String TELEMETRY_CONTEXT = "opentelemetry.context";
  private static final String TELEMETRY_SCOPE = "opentelemetry.scope";
  private static final Logger LOGGER = Logger.getLogger(OtelJaxrsClientFilter.class.getName());

  private final Tracer tracer;
  private final HttpClientHandler<ClientRequestContext, ClientResponseContext, ClientRequestContext>
      handler;

  /** Constructs a filter object. */
  public OtelJaxrsClientFilter() {
    this(
        new UrlPathDrivenJaxrsClientHttpExtractor(),
        new HttpStatus2OtStatusConverter(),
        OpenTelemetry.getTracerFactory().get(INSTRUMENTATION_LIB_ID));
  }

  /**
   * Constructs a filter object.
   *
   * @param httpExtractor the JAX-RS specific HTTP value extractor
   * @param statusConverter the HTTP to Otel status converter
   * @param tracer the tracer
   */
  public OtelJaxrsClientFilter(
      HttpExtractor<ClientRequestContext, ClientResponseContext> httpExtractor,
      HttpStatus2OtStatusConverter statusConverter,
      Tracer tracer) {
    super();
    this.tracer = tracer;
    HttpTextFormat.Setter<ClientRequestContext> setter =
        new Setter<ClientRequestContext>() {
          @Override
          public void put(ClientRequestContext carrier, String key, String value) {
            carrier.getHeaders().putSingle(key, value);
          }
        };
    handler =
        new HttpClientHandler<>(
            httpExtractor,
            setter,
            statusConverter,
            tracer,
            OpenTelemetry.getDistributedContextManager(),
            OpenTelemetry.getMeterFactory().get(INSTRUMENTATION_LIB_ID));
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public void filter(ClientRequestContext requestContext) {
    HttpRequestContext context = handler.handleStart(null, null, requestContext, requestContext);
    requestContext.setProperty(TELEMETRY_CONTEXT, context);
    Span span = handler.getSpanFromContext(context);
    requestContext.setProperty(TELEMETRY_SCOPE, tracer.withSpan(span));
    LOGGER.log(
        Level.FINER,
        "calling " + requestContext.getUri() + " with trace span " + span.getContext());
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
    HttpRequestContext context = (HttpRequestContext) requestContext.getProperty(TELEMETRY_CONTEXT);
    try (Scope scope = (Scope) requestContext.getProperty(TELEMETRY_SCOPE)) {
      handler.handleEnd(context, requestContext, responseContext, null);
    }
  }
}
