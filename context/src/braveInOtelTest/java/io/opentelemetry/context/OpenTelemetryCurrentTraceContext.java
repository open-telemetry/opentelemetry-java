/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;

public class OpenTelemetryCurrentTraceContext extends CurrentTraceContext {

  private static final ContextKey<TraceContext> TRACE_CONTEXT_KEY =
      ContextKey.named("brave-tracecontext");

  @Override
  public TraceContext get() {
    return Context.current().getValue(TRACE_CONTEXT_KEY);
  }

  @SuppressWarnings("ReferenceEquality")
  @Override
  public Scope newScope(TraceContext context) {
    Context currentOtel = Context.current();
    TraceContext currentBrave = currentOtel.getValue(TRACE_CONTEXT_KEY);
    if (currentBrave == context) {
      return Scope.NOOP;
    }

    Context newOtel = currentOtel.withValues(TRACE_CONTEXT_KEY, context);
    io.opentelemetry.context.Scope otelScope = newOtel.makeCurrent();
    return otelScope::close;
  }
}
