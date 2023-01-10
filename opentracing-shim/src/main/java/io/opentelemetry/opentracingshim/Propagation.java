/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

final class Propagation {
  private static final TextMapSetter SETTER_INSTANCE = new TextMapSetter();
  private static final TextMapGetter GETTER_INSTANCE = new TextMapGetter();

  private final TextMapPropagator textMapPropagator;
  private final TextMapPropagator httpPropagator;

  Propagation(TextMapPropagator textMapPropagator, TextMapPropagator httpPropagator) {
    this.textMapPropagator = textMapPropagator;
    this.httpPropagator = httpPropagator;
  }

  <C> void injectTextMap(SpanContextShim contextShim, Format<C> format, TextMapInject carrier) {
    Context context = Context.current().with(Span.wrap(contextShim.getSpanContext()));
    context = context.with(contextShim.getBaggage());

    getPropagator(format).inject(context, carrier, SETTER_INSTANCE);
  }

  @Nullable
  <C> SpanContextShim extractTextMap(Format<C> format, TextMapExtract carrier) {
    Map<String, String> carrierMap = new HashMap<>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    Context context = getPropagator(format).extract(Context.current(), carrierMap, GETTER_INSTANCE);

    Span span = Span.fromContext(context);
    Baggage baggage = Baggage.fromContext(context);
    if (!span.getSpanContext().isValid() && baggage.isEmpty()) {
      return null;
    }

    return new SpanContextShim(span.getSpanContext(), baggage);
  }

  // Visible for testing
  <C> TextMapPropagator getPropagator(Format<C> format) {
    if (format == Format.Builtin.HTTP_HEADERS) {
      return httpPropagator;
    }
    return textMapPropagator;
  }

  static final class TextMapSetter
      implements io.opentelemetry.context.propagation.TextMapSetter<TextMapInject> {
    private TextMapSetter() {}

    @Override
    public void set(@Nullable TextMapInject carrier, String key, String value) {
      if (carrier != null) {
        carrier.put(key, value);
      }
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter
      implements io.opentelemetry.context.propagation.TextMapGetter<Map<String, String>> {
    private TextMapGetter() {}

    @Override
    public Iterable<String> keys(Map<String, String> carrier) {
      return carrier.keySet();
    }

    @Override
    @Nullable
    public String get(@Nullable Map<String, String> carrier, String key) {
      if (carrier == null) {
        return null;
      }
      for (Map.Entry<String, String> entry : carrier.entrySet()) {
        if (key.equalsIgnoreCase(entry.getKey())) {
          return entry.getValue();
        }
      }
      return null;
    }
  }
}
