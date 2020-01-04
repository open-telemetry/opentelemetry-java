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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryMetadata.EntryTtl;
import io.opentelemetry.distributedcontext.EntryValue;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.Tracestate;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This helper class provides routine methods to instrument HTTP servers.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @param <C> the type of the carrier.
 */
public class HttpServerHandler<Q, P, C> extends AbstractHttpHandler<Q, P> {

  private static final Logger LOGGER = Logger.getLogger(HttpServerHandler.class.getName());

  private final Tracer tracer;
  private final DistributedContextManager contextManager;
  private final HttpTextFormat.Getter<C> getter;
  private final boolean publicEndpoint;
  private final HttpTextFormat<SpanContext> spanContextHttpTextFormat;
  private final HttpTextFormat<DistributedContext> distributedContextHttpTextFormat;

  /**
   * Constructs a handler object.
   *
   * @param extractor used to extract information from request/response
   * @param getter the getter used to extract information from the carrier
   */
  public HttpServerHandler(HttpExtractor<Q, P> extractor, HttpTextFormat.Getter<C> getter) {
    this(extractor, getter, null, null, null, null, null);
  }

  /**
   * Constructs a handler object.
   *
   * @param extractor used to extract information from request/response
   * @param getter the getter used to extract information from the carrier
   * @param statusConverter the HTTP status to OT status translator
   * @param tracer the OT tracer
   * @param contextManager the OT distributed context manager
   * @param meter the OT meter
   * @param publicEndpoint whether a new trace should be started for all requests or not
   */
  public HttpServerHandler(
      HttpExtractor<Q, P> extractor,
      HttpTextFormat.Getter<C> getter,
      StatusCodeConverter statusConverter,
      Tracer tracer,
      DistributedContextManager contextManager,
      Meter meter,
      Boolean publicEndpoint) {
    super(extractor, statusConverter, meter);
    checkNotNull(getter, "getter is required");
    if (tracer == null) {
      this.tracer = OpenTelemetry.getTracerFactory().get(INSTRUMENTATION_LIB_ID);
    } else {
      this.tracer = tracer;
    }
    if (contextManager == null) {
      this.contextManager = OpenTelemetry.getDistributedContextManager();
    } else {
      this.contextManager = contextManager;
    }
    this.getter = getter;
    if (publicEndpoint == null) {
      this.publicEndpoint = false;
    } else {
      this.publicEndpoint = publicEndpoint;
    }
    this.spanContextHttpTextFormat = this.tracer.getHttpTextFormat();
    //    this.distributedContextHttpTextFormat = this.contextManager.getHttpTextFormat();
    this.distributedContextHttpTextFormat = new TemporaryDistributedContextTextFormat();
  }

  /**
   * Instrument an incoming request before it is handled.
   *
   * <p>This method will create a span under the deserialized propagated parent context. If the
   * parent context is not present, the span will be created under the current context.
   *
   * <p>The generated span will NOT be set as current context. User can control when to enter the
   * scope of this span. Use getSpanFromContext to retrieve the span.
   *
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return the wrapper for all request info
   */
  public HttpRequestContext handleStart(C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    Span.Builder spanBuilder = tracer.spanBuilder(getSpanName(request));
    DistributedContext.Builder dctxBuilder = contextManager.contextBuilder();

    SpanContext spanContext = null;
    try {
      spanContext = spanContextHttpTextFormat.extract(carrier, getter);
    } catch (IllegalArgumentException ignore) {
      // NoOp
    }
    DistributedContext distContext = null;
    try {
      distContext = distributedContextHttpTextFormat.extract(carrier, getter);
    } catch (IllegalArgumentException ignore) {
      // NoOp
    }
    if (spanContext == null || publicEndpoint) {
      spanBuilder.setNoParent();
      dctxBuilder.setNoParent();
    } else {
      spanBuilder.setParent(spanContext);
      if (distContext == null) {
        dctxBuilder.setParent(contextManager.getCurrentContext());
      } else {
        dctxBuilder.setParent(distContext);
      }
      addTracestateToDistributedContext(dctxBuilder, spanContext.getTracestate());
    }

    Span span = spanBuilder.setSpanKind(Kind.SERVER).startSpan();
    if (publicEndpoint && spanContext != null) {
      span.setAttribute(HttpTraceConstants.LINK_TYPE, HttpTraceConstants.LINK_ATTR_ORIGINATING);
    }

    addSpanRequestAttributes(span, request);
    return getNewContext(span, dctxBuilder.build());
  }

  private static void addTracestateToDistributedContext(
      DistributedContext.Builder dctxBuilder, Tracestate tracestate) {
    if (tracestate != null) {
      for (Tracestate.Entry entry : tracestate.getEntries()) {
        dctxBuilder.put(
            EntryKey.create(entry.getKey()),
            EntryValue.create(entry.getValue()),
            EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION));
      }
    }
  }

  /**
   * Close an HTTP span and records events and measurements specific to the request.
   *
   * <p>This method will set status of the span and end it. Additionally it will record message
   * events for the span and record measurements associated with the request.
   *
   * @param context the context from handle start
   * @param request the request object
   * @param response the response object
   * @param error exception caught while waiting for or processing response
   */
  public void handleEnd(
      HttpRequestContext context, Q request, @Nullable P response, @Nullable Throwable error) {
    checkNotNull(context, "context");
    checkNotNull(request, "request");
    int httpCode = extractor.getStatusCode(response);
    recordMeasurements(context, httpCode);
    spanEnd(context.span, httpCode, error);
  }

  @Override
  Logger getLogger() {
    return LOGGER;
  }
}
