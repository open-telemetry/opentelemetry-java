/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.opencensusshim.SpanConverter.mapSpanContext;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.TextFormat;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

class OpenTelemetryTextFormatImpl extends TextFormat {

  private final TextMapPropagator propagator;

  OpenTelemetryTextFormatImpl(TextMapPropagator propagator) {
    this.propagator = propagator;
  }

  @Override
  public List<String> fields() {
    return new ArrayList<>(propagator.fields());
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    io.opentelemetry.api.trace.SpanContext otelSpanContext = mapSpanContext(spanContext);
    Context otelContext = Context.current().with(Span.wrap(otelSpanContext));
    propagator.inject(otelContext, carrier, setter::put);
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) {
    Context context =
        propagator.extract(
            Context.current(),
            carrier,
            new TextMapGetter<C>() {
              // OC Getter cannot return keys for an object, but users should not need it either.
              @Override
              public Iterable<String> keys(C carrier) {
                return Collections.emptyList();
              }

              @Nullable
              @Override
              public String get(@Nullable C carrier, String key) {
                return getter.get(carrier, key);
              }
            });
    io.opentelemetry.api.trace.SpanContext spanContext = Span.fromContext(context).getSpanContext();
    return mapSpanContext(spanContext);
  }
}
