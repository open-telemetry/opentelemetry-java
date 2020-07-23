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

package io.opentelemetry.extensions.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorInjectorMultipleHeaders implements B3PropagatorInjector {
  @Override
  public <C> void inject(Context context, C carrier, HttpTextFormat.Setter<C> setter) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(setter, "setter");

    Span span = TracingContextUtils.getSpanWithoutDefault(context);
    if (span == null || !span.getContext().isValid()) {
      return;
    }

    SpanContext spanContext = span.getContext();
    String sampled = spanContext.getTraceFlags().isSampled() ? Common.TRUE_INT : Common.FALSE_INT;

    setter.set(carrier, B3Propagator.TRACE_ID_HEADER, spanContext.getTraceId().toLowerBase16());
    setter.set(carrier, B3Propagator.SPAN_ID_HEADER, spanContext.getSpanId().toLowerBase16());
    setter.set(carrier, B3Propagator.SAMPLED_HEADER, sampled);
  }
}
