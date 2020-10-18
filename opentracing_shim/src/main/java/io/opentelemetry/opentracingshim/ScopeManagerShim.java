/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.trace.TracingContextUtils;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

final class ScopeManagerShim extends BaseShimObject implements ScopeManager {

  public ScopeManagerShim(TelemetryInfo telemetryInfo) {
    super(telemetryInfo);
  }

  @Override
  @SuppressWarnings("ReturnMissingNullable")
  public Span activeSpan() {
    // As OpenTracing simply returns null when no active instance is available,
    // we need to do map an invalid OpenTelemetry span to null here.
    io.opentelemetry.trace.Span span = TracingContextUtils.getCurrentSpan();
    if (!span.getContext().isValid()) {
      return null;
    }

    // TODO: Properly include the bagagge/distributedContext.
    return new SpanShim(telemetryInfo(), span);
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public Scope activate(Span span) {
    io.opentelemetry.trace.Span actualSpan = getActualSpan(span);
    return new ScopeShim(TracingContextUtils.currentContextWith(actualSpan));
  }

  static io.opentelemetry.trace.Span getActualSpan(Span span) {
    if (!(span instanceof SpanShim)) {
      throw new IllegalArgumentException("span is not a valid SpanShim object");
    }

    return ((SpanShim) span).getSpan();
  }
}
