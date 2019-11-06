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
import io.opentelemetry.context.propagation.HttpExtractor;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.SpanContext;
import javax.annotation.Nullable;

public final class HttpTraceContextExtractor implements HttpExtractor {
  private static final HttpTraceContext PROPAGATOR = new HttpTraceContext();

  @Override
  public <C> Context extract(Context ctx, C carrier, Getter<C> getter) {
    SpanContext spanCtx = PROPAGATOR.extract(carrier, new GetterImpl<C>(getter));
    return ctx.setValue(ContextKeys.getSpanContextKey(), spanCtx);
  }

  // Utility class, not relevant.
  static final class GetterImpl<C> implements HttpTextFormat.Getter<C> {
    Getter<C> wrapped;

    GetterImpl(Getter<C> wrapped) {
      this.wrapped = wrapped;
    }

    @Nullable
    @Override
    public String get(C carrier, String key) {
      return wrapped.get(carrier, key);
    }
  }
}
