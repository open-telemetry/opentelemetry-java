/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorInjectorMultipleHeaders implements B3PropagatorInjector {
  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null) {
      return;
    }
    if (setter == null) {
      return;
    }

    SpanContext spanContext = Span.fromContext(context).getSpanContext();
    if (!spanContext.isValid()) {
      return;
    }

    String sampled = spanContext.isSampled() ? Common.TRUE_INT : Common.FALSE_INT;

    if (Boolean.TRUE.equals(context.get(B3Propagator.DEBUG_CONTEXT_KEY))) {
      setter.set(carrier, B3Propagator.DEBUG_HEADER, Common.TRUE_INT);
      sampled = Common.TRUE_INT;
    }

    setter.set(carrier, B3Propagator.TRACE_ID_HEADER, spanContext.getTraceId());
    setter.set(carrier, B3Propagator.SPAN_ID_HEADER, spanContext.getSpanId());
    setter.set(carrier, B3Propagator.SAMPLED_HEADER, sampled);
  }
}
