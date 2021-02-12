/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

final class Propagation extends BaseShimObject {
  Propagation(TelemetryInfo telemetryInfo) {
    super(telemetryInfo);
  }

  public void injectTextMap(SpanContextShim contextShim, TextMapInject carrier) {
    Context context = Context.current().with(Span.wrap(contextShim.getSpanContext()));
    context = context.with(contextShim.getBaggage());

    propagators().getTextMapPropagator().inject(context, carrier, TextMapSetter.INSTANCE);
  }

  @Nullable
  public SpanContextShim extractTextMap(TextMapExtract carrier) {
    Map<String, String> carrierMap = new HashMap<>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    Context context =
        propagators()
            .getTextMapPropagator()
            .extract(Context.current(), carrierMap, TextMapGetter.INSTANCE);

    Span span = Span.fromContext(context);
    if (!span.getSpanContext().isValid()) {
      return null;
    }

    return new SpanContextShim(telemetryInfo, span.getSpanContext(), Baggage.fromContext(context));
  }

  static final class TextMapSetter
      implements io.opentelemetry.context.propagation.TextMapSetter<TextMapInject> {
    private TextMapSetter() {}

    public static final TextMapSetter INSTANCE = new TextMapSetter();

    @Override
    public void set(TextMapInject carrier, String key, String value) {
      carrier.put(key, value);
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter
      implements io.opentelemetry.context.propagation.TextMapGetter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Nullable
    @Override
    public Iterable<String> keys(Map<String, String> carrier) {
      return carrier.keySet();
    }

    @Override
    public String get(Map<String, String> carrier, String key) {
      for (Map.Entry<String, String> entry : carrier.entrySet()) {
        if (key.equalsIgnoreCase(entry.getKey())) {
          return entry.getValue();
        }
      }
      return null;
    }
  }
}
