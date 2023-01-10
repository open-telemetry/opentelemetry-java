/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class TracerShim implements Tracer {
  private static final Logger logger = Logger.getLogger(TracerShim.class.getName());

  private final io.opentelemetry.api.trace.Tracer tracer;
  private final ScopeManager scopeManagerShim;
  private final Propagation propagation;
  private volatile boolean isClosed;

  TracerShim(
      io.opentelemetry.api.trace.Tracer tracer, OpenTracingPropagators openTracingPropagators) {
    this.tracer = tracer;
    this.propagation = new Propagation(openTracingPropagators);
    this.scopeManagerShim = new ScopeManagerShim();
  }

  // Visible for testing
  io.opentelemetry.api.trace.Tracer tracer() {
    return tracer;
  }

  // Visible for testing
  Propagation propagation() {
    return propagation;
  }

  @Override
  public ScopeManager scopeManager() {
    return scopeManagerShim;
  }

  @Override
  public Span activeSpan() {
    return scopeManagerShim.activeSpan();
  }

  @Override
  public Scope activateSpan(Span span) {
    return scopeManagerShim.activate(span);
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    if (isClosed) {
      return new NoopSpanBuilderShim(operationName);
    }

    return new SpanBuilderShim(tracer, operationName);
  }

  @Override
  public <C> void inject(SpanContext context, Format<C> format, C carrier) {
    if (context == null) {
      logger.log(Level.INFO, "Cannot inject a null span context.");
      return;
    }

    SpanContextShim contextShim = ShimUtil.getContextShim(context);
    if (contextShim == null) {
      return;
    }

    if (format == Format.Builtin.TEXT_MAP
        || format == Format.Builtin.TEXT_MAP_INJECT
        || format == Format.Builtin.HTTP_HEADERS) {
      propagation.injectTextMap(contextShim, format, (TextMapInject) carrier);
    }
  }

  @Override
  @Nullable
  public <C> SpanContext extract(Format<C> format, C carrier) {
    try {
      if (format == Format.Builtin.TEXT_MAP
          || format == Format.Builtin.TEXT_MAP_EXTRACT
          || format == Format.Builtin.HTTP_HEADERS) {
        return propagation.extractTextMap(format, (TextMapExtract) carrier);
      }
    } catch (RuntimeException e) {
      logger.log(
          Level.INFO,
          "Exception caught while extracting span context; returning null. "
              + "Exception: [{0}] Message: [{1}]",
          new String[] {e.getClass().getName(), e.getMessage()});
    }

    return null;
  }

  @Override
  public void close() {
    isClosed = true;
  }
}
