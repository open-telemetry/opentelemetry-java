/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Span;
import io.opentelemetry.api.trace.TracingContextUtils;
import io.opentelemetry.context.Context;

public class OpenTelemetryContextManager implements ContextManager {

  private final SpanCache spanCache;

  public OpenTelemetryContextManager() {
    this.spanCache = SpanCache.getInstance();
  }

  @Override
  public ContextHandle currentContext() {
    return wrapContext(Context.current());
  }

  @Override
  public ContextHandle withValue(ContextHandle ctx, Span span) {
    OpenTelemetryCtx openTelemetryCtx = (OpenTelemetryCtx) ctx;
    return wrapContext(
        TracingContextUtils.withSpan(spanCache.toOtelSpan(span), unwrapContext(openTelemetryCtx)));
  }

  @Override
  public Span getValue(ContextHandle ctx) {
    return spanCache.fromOtelSpan(TracingContextUtils.getSpan(unwrapContext(ctx)));
  }

  private static ContextHandle wrapContext(Context context) {
    return new OpenTelemetryCtx(context);
  }

  private static Context unwrapContext(ContextHandle ctx) {
    return ((OpenTelemetryCtx) ctx).getContext();
  }
}
