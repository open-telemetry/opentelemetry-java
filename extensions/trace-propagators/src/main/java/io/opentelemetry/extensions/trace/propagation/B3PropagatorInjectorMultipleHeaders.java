/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
final class B3PropagatorInjectorMultipleHeaders implements B3PropagatorInjector {
  @Override
  public <C> void inject(Context context, C carrier, TextMapPropagator.Setter<C> setter) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(setter, "setter");

    SpanContext spanContext = Span.fromContext(context).getSpanContext();
    if (!spanContext.isValid()) {
      return;
    }

    String sampled = spanContext.isSampled() ? Common.TRUE_INT : Common.FALSE_INT;

    setter.set(carrier, B3Propagator.TRACE_ID_HEADER, spanContext.getTraceIdAsHexString());
    setter.set(carrier, B3Propagator.SPAN_ID_HEADER, spanContext.getSpanIdAsHexString());
    setter.set(carrier, B3Propagator.SAMPLED_HEADER, sampled);
  }
}
