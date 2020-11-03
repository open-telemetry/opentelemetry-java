/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.google.errorprone.annotations.MustBeClosed;

public class OpenTelemetryCurrentTraceContext extends CurrentTraceContext {

  private static final ContextKey<TraceContext> TRACE_CONTEXT_KEY =
      ContextKey.named("brave-tracecontext");

  @Override
  public TraceContext get() {
    return Context.current().get(TRACE_CONTEXT_KEY);
  }

  @SuppressWarnings({"ReferenceEquality", "MustBeClosedChecker"})
  @Override
  @MustBeClosed
  public Scope newScope(TraceContext context) {
    Context currentOtel = Context.current();
    TraceContext currentBrave = currentOtel.get(TRACE_CONTEXT_KEY);
    if (currentBrave == context) {
      return Scope.NOOP;
    }

    Context newOtel = currentOtel.with(TRACE_CONTEXT_KEY, context);
    io.opentelemetry.context.Scope otelScope = newOtel.makeCurrent();
    return otelScope::close;
  }
}
