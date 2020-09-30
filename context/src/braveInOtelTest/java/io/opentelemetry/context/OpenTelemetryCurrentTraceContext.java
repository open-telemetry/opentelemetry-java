/*
 * Copyright 2020, OpenTelemetry Authors
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
