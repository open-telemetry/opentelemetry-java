/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
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
    Context context =
        TracingContextUtils.withSpan(
            DefaultSpan.create(contextShim.getSpanContext()), Context.current());
    context = BaggageUtils.withBaggage(contextShim.getBaggage(), context);

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

    Span span = TracingContextUtils.getSpan(context);
    if (!span.getContext().isValid()) {
      return null;
    }

    return new SpanContextShim(telemetryInfo, span.getContext(), BaggageUtils.getBaggage(context));
  }

  static final class TextMapSetter implements TextMapPropagator.Setter<TextMapInject> {
    private TextMapSetter() {}

    public static final TextMapSetter INSTANCE = new TextMapSetter();

    @Override
    public void set(TextMapInject carrier, String key, String value) {
      carrier.put(key, value);
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter implements TextMapPropagator.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }
}
