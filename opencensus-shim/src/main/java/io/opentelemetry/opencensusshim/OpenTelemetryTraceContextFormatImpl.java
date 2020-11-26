/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.opencensusshim.SpanConverter.mapSpanContext;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.TextFormat;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class OpenTelemetryTraceContextFormatImpl extends TextFormat {
  private static final HttpTraceContext OTEL_TRACE_CONTEXT_FORMAT_IMPL =
      HttpTraceContext.getInstance();

  @Override
  public List<String> fields() {
    return OTEL_TRACE_CONTEXT_FORMAT_IMPL.fields();
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    io.opentelemetry.api.trace.SpanContext otelSpanContext = mapSpanContext(spanContext);
    Context otelContext = Context.current().with(Span.wrap(otelSpanContext));
    OTEL_TRACE_CONTEXT_FORMAT_IMPL.inject(otelContext, carrier, setter::put);
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) {
    Context context =
        OTEL_TRACE_CONTEXT_FORMAT_IMPL.extract(
            Context.current(),
            carrier,
            new TextMapPropagator.Getter<C>() {
              @Override
              public Iterable<String> keys(C carrier) {
                return new ArrayList<>();
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
