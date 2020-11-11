/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Span;
import io.opentelemetry.context.Context;
import java.util.logging.Logger;

/**
 * This is a context manager implementation that overrides the default OpenCensus context manager
 * {@link io.opencensus.trace.unsafe.ContextManagerImpl}. It is loaded by OpenCensus via reflection
 * automatically in {@link io.opencensus.trace.unsafe.ContextHandleUtils} when the OpenCensus shim
 * library exists as a dependency.
 */
public class OpenTelemetryContextManager implements ContextManager {

  private static final Logger LOGGER =
      Logger.getLogger(OpenTelemetryContextManager.class.getName());

  public OpenTelemetryContextManager() {}

  @Override
  public ContextHandle currentContext() {
    return wrapContext(Context.current());
  }

  @Override
  public ContextHandle withValue(ContextHandle ctx, Span span) {
    if (!(ctx instanceof OpenTelemetryCtx)) {
      LOGGER.warning(
          "ContextHandle is not an instance of OpenTelemetryCtx. "
              + "There should not be a local implementation of ContextHandle "
              + "other than OpenTelemetryCtx.");
    }
    OpenTelemetryCtx openTelemetryCtx = (OpenTelemetryCtx) ctx;
    return wrapContext(unwrapContext(openTelemetryCtx).with((OpenTelemetrySpanImpl) span));
  }

  @Override
  public Span getValue(ContextHandle ctx) {
    io.opentelemetry.api.trace.Span span = io.opentelemetry.api.trace.Span.fromContext(unwrapContext(ctx));
    if (span instanceof OpenTelemetrySpanImpl) {
      return (OpenTelemetrySpanImpl) span;
    }
    return SpanConverter.fromOtelSpan(span);
  }

  private static ContextHandle wrapContext(Context context) {
    return new OpenTelemetryCtx(context);
  }

  private static Context unwrapContext(ContextHandle ctx) {
    return ((OpenTelemetryCtx) ctx).getContext();
  }
}
