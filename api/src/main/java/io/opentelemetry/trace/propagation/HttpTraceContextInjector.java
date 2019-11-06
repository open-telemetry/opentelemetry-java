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

package io.opentelemetry.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.HttpInjector;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.SpanContext;

public final class HttpTraceContextInjector implements HttpInjector {
  private static final HttpTraceContext PROPAGATOR = new HttpTraceContext();

  @Override
  public <C> void inject(Context ctx, C carrier, Setter<C> setter) {
    SpanContext spanCtx = ctx.getValue(ContextKeys.getSpanContextKey());
    if (spanCtx == null) {
      return;
    }

    PROPAGATOR.inject(spanCtx, carrier, new SetterImpl<C>(setter));
  }

  // Utility class, not relevant.
  static final class SetterImpl<C> implements HttpTextFormat.Setter<C> {
    Setter<C> wrapped;

    SetterImpl(Setter<C> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public void put(C carrier, String key, String value) {
      wrapped.put(carrier, key, value);
    }
  }
}
