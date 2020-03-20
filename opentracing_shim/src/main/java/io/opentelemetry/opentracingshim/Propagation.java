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

package io.opentelemetry.opentracingshim;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.correlationcontext.CorrelationsContextUtils;
import io.opentelemetry.trace.DefaultSpan;
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

  public void injectTextFormat(SpanContextShim contextShim, TextMapInject carrier) {
    Context context =
        TracingContextUtils.withSpan(
            DefaultSpan.create(contextShim.getSpanContext()), Context.current());
    context =
        CorrelationsContextUtils.withCorrelationContext(
            contextShim.getCorrelationContext(), context);

    propagators().getHttpTextFormat().inject(context, carrier, TextMapSetter.INSTANCE);
  }

  @Nullable
  public SpanContextShim extractTextFormat(TextMapExtract carrier) {
    Map<String, String> carrierMap = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : carrier) {
      carrierMap.put(entry.getKey(), entry.getValue());
    }

    Context context =
        propagators()
            .getHttpTextFormat()
            .extract(Context.current(), carrierMap, TextMapGetter.INSTANCE);

    io.opentelemetry.trace.Span span = TracingContextUtils.getSpan(context);
    if (!span.getContext().isValid()) {
      return null;
    }

    return new SpanContextShim(
        telemetryInfo, span.getContext(), CorrelationsContextUtils.getCorrelationContext(context));
  }

  static final class TextMapSetter implements HttpTextFormat.Setter<TextMapInject> {
    private TextMapSetter() {}

    public static final TextMapSetter INSTANCE = new TextMapSetter();

    @Override
    public void set(TextMapInject carrier, String key, String value) {
      carrier.put(key, value);
    }
  }

  // We use Map<> instead of TextMap as we need to query a specified key, and iterating over
  // *all* values per key-query *might* be a bad idea.
  static final class TextMapGetter implements HttpTextFormat.Getter<Map<String, String>> {
    private TextMapGetter() {}

    public static final TextMapGetter INSTANCE = new TextMapGetter();

    @Override
    public String get(Map<String, String> carrier, String key) {
      return carrier.get(key);
    }
  }
}
